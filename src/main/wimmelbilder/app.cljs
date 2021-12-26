(ns wimmelbilder.app)

(defn root
  []
  (js/document.getElementById "root"))

(defn element
  [element-type & [attributes]]
  (let [e (js/document.createElement element-type)]
    (js/Object.assign e (clj->js attributes))))

(defn canvas
  []
  (first (.getElementsByTagName (root) "canvas")))

(defn replace-canvas!
  [width height]
  (when-let [c (canvas)]
    (.remove c))
  (.appendChild
    (root)
    (element "canvas" {:id "canvas" :width width, :height height})))

(defn load-image!
  [image-path callback]
  (let [image (element "img" {:src image-path})]
    (.addEventListener image "load" #(callback image))))

;; TODO: When support for OffscreenCanvas is added to Firefox, we could optimize
;; this by drawing the image to an offscreen canvas with scaling and then
;; drawing it to the actual canvas.
;;
;; Reference:
;; https://developer.mozilla.org/en-US/docs/Web/API/Canvas_API/Tutorial/Optimizing_canvas
(defn draw-image!
  [ctx image x y & [scale-factor]]
  (.drawImage
    ctx
    image
    x
    y
    (* (.-width image) (or scale-factor 1))
    (* (.-height image) (or scale-factor 1))))

;; TODO: re-think this
(defn random-opaque-coordinate
  "Returns a [width height] tuple for a point on the canvas that is
   non-transparent."
  []
  (let [canvas
        (canvas)

        ctx
        (.getContext canvas "2d")

        random-coordinates
        (fn []
          [(rand-int (.-width canvas))
           (rand-int (.-height canvas))])]
    (random-coordinates)
    #_(loop [[x y] (random-coordinates)]
        (let [image-data (.-data (.getImageData ctx x y 1 1))
              r          (aget image-data 0)
              g          (aget image-data 1)
              b          (aget image-data 2)
              a          (aget image-data 3)]
          (if (and (pos? a) (not= [0 0 0] [r g b]))
            [x y]
            (recur (random-coordinates)))))))

(defn window-dimensions
  []
  (let [width  (or (.. js/window -innerWidth)
                   (.. js/document -documentElement -clientWidth)
                   (.. js/document -body -clientWidth))
        height (or (.. js/window -innerHeight)
                   (.. js/document -documentElement -clientHeight)
                   (.. js/document -body -clientHeight))]
    [width height]))

(defn scaled-to-fit-screen
  [image-width image-height]
  (let [[screen-width screen-height]
        (window-dimensions)

        width-scale-factor
        (/ screen-width image-width)

        height-scale-factor
        (/ screen-height image-height)

        scale-factor
        (min width-scale-factor height-scale-factor)]
    [(* image-width scale-factor)
     (* image-height scale-factor)
     scale-factor]))

(defn render-app!
  []
  (load-image!
    "img/bg/2.jpeg"
    (fn [bg-image]
      (let [[width height scale-factor]
            (scaled-to-fit-screen (.-width bg-image) (.-height bg-image))

            _
            (replace-canvas! width height)

            ctx
            (.getContext (canvas) "2d")]
        (draw-image! ctx bg-image 0 0 scale-factor)
        (load-image!
          "img/sprite/doge.png"
          (fn [doge-image]
            (load-image!
              "img/sprite/doge-flipped.png"
              (fn [doge-flipped-image]
                (dotimes [_ (* 300 scale-factor)]
                  (let [[x y] (random-opaque-coordinate)
                        image (if (< (rand) 0.5)
                                doge-image
                                doge-flipped-image)]
                    (draw-image!
                      ctx
                      image
                      x
                      y
                      0.075)))))))
        (load-image!
          "img/sprite/doge-scarf.png"
          (fn [doge-image]
            (let [[width height] (random-opaque-coordinate)]
              (draw-image!
                ctx
                doge-image
                width
                height
                0.2))))))))

(defn init []
  (println "init")
  (let [render! #(js/window.setTimeout render-app! 0)]
    (if (= "complete" (.-readyState js/document))
      (render!)
      (.addEventListener js/window "load" render!))))

(defn reload []
  (println "reload")
  (render-app!))
