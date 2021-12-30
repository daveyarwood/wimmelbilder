(ns wimmelbilder.app
  (:require [clojure.core.async :as async])
  (:require-macros [wimmelbilder.app :as app]))

(defn root
  []
  (js/document.getElementById "root"))

(defn element
  [element-type & [attributes]]
  (let [e (js/document.createElement element-type)]
    (js/Object.assign e (clj->js attributes))))

(def image-files
  (app/image-files))

(def background-files
  (map #(str "img/bg/" %)
       (app/background-files)))

(def target-image
  "img/sprite/doge-scarf.png")

(def other-images
  (->> image-files
       (map #(str "img/sprite/" %))
       (filter #(not= target-image %))))

(defn load-image!
  [image-path]
  (let [ch    (async/chan)
        image (element "img" {:src image-path})]
    (.addEventListener image "load" #(async/put! ch image))
    ch))

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

(def bg-specific-sprite-scale-factors
  {#_#_"img/bg/4.png" 5})

(defn render-app!
  []
  (async/go
    (let [background-file
          (rand-nth background-files)

          _
          (prn :background-file background-file)

          bg-sprite-scale-factor
          (* (get bg-specific-sprite-scale-factors background-file 1)
             ;; TODO: Replace this with an image-specific scale factor?
             1.0)

          bg-image-ch
          (load-image! background-file)

          target-image-ch
          (load-image! target-image)

          other-images-chs
          (map load-image! other-images)

          other-images
          (atom [])

          _
          (dotimes [_ (count other-images-chs)]
            (let [[image _channel] (async/alts! other-images-chs)]
              (swap! other-images concat [image])))

          bg-image
          (async/<! bg-image-ch)

          target-image
          (async/<! target-image-ch)

          other-images
          @other-images

          [width height bg-scale-factor]
          (scaled-to-fit-screen (.-width bg-image) (.-height bg-image))

          _
          (replace-canvas! width height)

          ctx
          (.getContext (canvas) "2d")

          _
          (draw-image! ctx bg-image 0 0 bg-scale-factor)]
       (dotimes [_ (* 300 bg-scale-factor)]
         (let [[x y] (random-opaque-coordinate)
               image (rand-nth other-images)]
           (draw-image!
             ctx
             image
             x
             y
             (* 0.075 bg-scale-factor bg-sprite-scale-factor))))
       (let [[width height] (random-opaque-coordinate)]
         (draw-image!
           ctx
           target-image
           width
           height
           (* 0.2 bg-scale-factor bg-sprite-scale-factor))))))

(defn init []
  (println "init")
  (let [render! #(js/window.setTimeout render-app! 0)]
    (if (= "complete" (.-readyState js/document))
      (render!)
      (.addEventListener js/window "load" render!))))

(defn reload []
  (println "reload")
  (render-app!))
