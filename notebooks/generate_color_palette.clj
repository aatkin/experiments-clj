(ns generate-color-palette
  (:require [clojure.math]
            [garden.color :as c]
            [garden.arithmetic :as a]
            [nextjournal.clerk :as clerk]))

;; ^{::clerk/visibility {:result :hide}}

#_(defn mix-colors [c1 c2 w]
    (let [w1 (round-double (- 1.0 w))
          w2 w
          color (a/+ (a/* (c/hex->rgb c1) w1)
                     (a/* (c/hex->rgb c2) w2))]
      (->> (get-rgb color)
           (mapv clojure.math/round)
           (apply c/rgb))))

^{::clerk/visibility {:code :hide}}
(clerk/html
 [:details
  [:summary "Color palette"]

  [:p "content"]])
