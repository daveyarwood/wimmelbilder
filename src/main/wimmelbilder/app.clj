(ns wimmelbilder.app
  (:require [clojure.java.io :as io]))

(defmacro image-files
  []
  (vec (.list (io/file "public/img/sprite"))))

(defmacro background-files
  []
  (vec (.list (io/file "public/img/bg"))))
