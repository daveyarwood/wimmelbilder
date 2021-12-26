(ns wimmelbilder.app)

(defn element
  [element-type & [attributes]]
  (let [e (js/document.createElement element-type)]
    (js/Object.assign e (clj->js attributes))))

(def canvas (js/document.getElementById "canvas"))

(defn clear-canvas!
  []
  (let [ctx (.getContext canvas "2d")]
    (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))))

(defn load-image!
  [image-path callback]
  (let [image (element "img" {:src image-path})]
    (.addEventListener image "load" #(callback image))))

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
  []
  (let [ctx
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

(defn render-app!
  []
  (clear-canvas!)
  (load-image!
    "img/forest.gif"
    (fn [forest-image]
      (let [ctx (.getContext canvas "2d")]
        (draw-image! ctx forest-image 0 0)
        (load-image!
          "img/doge.png"
          (fn [doge-image]
            (load-image!
              "img/doge-flipped.png"
              (fn [doge-flipped-image]
                (dotimes [_ 100]
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
          "img/doge-scarf.png"
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
