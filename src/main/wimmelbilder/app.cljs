(ns wimmelbilder.app)

(defn root
  []
  (js/document.getElementById "root"))

(defn clear-root!
  []
  (let [root-node (root)]
    (while (.-firstChild (root))
      (.removeChild root-node (.-lastChild root-node)))))

(defn add-canvas!
  []
  (let [canvas (js/document.createElement "canvas")
        _      (set! (.-id canvas) "canvas")
        ;; TODO: dynamically re-draw everything based on the window size
        _      (set! (.-width canvas) 800)
        _      (set! (.-height canvas) 800)]
    (.appendChild (root) canvas)))

(defn canvas
  []
  (js/document.getElementById "canvas"))

(defn img
  [src]
  (let [element (js/document.createElement "img")]
    (set! (.-src element) src)
    element))

(defn load-image!
  [image-path callback]
  (let [image (img image-path)]
    (.addEventListener image "load" (callback image))))

(defn draw-image!
  [ctx image x y & [scale-factor]]
  (.drawImage
    ctx
    image
    x
    y
    (* (.-width image) (or scale-factor 1))
    (* (.-height image) (or scale-factor 1))))

(defn random-opaque-coordinate
  "Returns a [width height] tuple for a point on the canvas that is
   non-transparent."
  [canvas]
  (let [ctx
        (.getContext canvas "2d")

        random-coordinates
        (fn []
          [(rand-int (.-width canvas))
           (rand-int (.-height canvas))])]
    (loop [[x y] (random-coordinates)]
      (let [image-data (.-data (.getImageData ctx x y 1 1))
            r          (aget image-data 0)
            g          (aget image-data 1)
            b          (aget image-data 2)
            a          (aget image-data 3)]
        (if (and (pos? a) (not= [0 0 0] [r g b]))
          [x y]
          (recur (random-coordinates)))))))

(defn render-app!
  []
  (clear-root!)
  (add-canvas!)
  (load-image!
    "img/forest.gif"
    (fn [forest-image]
      (let [ctx (.getContext (canvas) "2d")]
        (draw-image! ctx forest-image 0 0)
        (dotimes [_ 100]
          (load-image!
            (if (< (rand) 0.5)
              "img/doge.png"
              "img/doge-flipped.png")
            (fn [doge-image]
              (let [[width height] (random-opaque-coordinate (canvas))]
                (draw-image!
                  ctx
                  doge-image
                  width
                  height
                  0.075)))))
        (load-image!
          "img/doge-scarf.png"
          (fn [doge-image]
            (let [[width height] (random-opaque-coordinate (canvas))]
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
