^{:nextjournal.clerk/visibility {:code :hide}}
(ns experiments.generate-color-palette
  (:require [clojure.math]
            [garden.color :as c]
            [garden.arithmetic :as a]
            [nextjournal.clerk :as clerk]
            [experiments.contrast-ratio :refer [get-contrast-ratio]]))

{::clerk/visibility {:code :hide
                     :result :hide}}

(defn round-double [d & [precision]]
  (->> (to-array [d])
       (java.lang.String/format java.util.Locale/US
                                (if precision
                                  (str "%." precision "f")
                                  "%.3f"))
       (parse-double)))

(def get-rgb (juxt :red :green :blue))

(defn mix-colors [c1 c2 w]
  (let [w1 (round-double (- 1.0 w))
        w2 w
        color (a/+ (a/* (c/hex->rgb c1) w1)
                   (a/* (c/hex->rgb c2) w2))]
    (->> (get-rgb color)
         (mapv clojure.math/round)
         (apply c/rgb))))

(defn generate-color-shades [color color-dark]
  (let [light-colors (for [[i weight] {100 0.125
                                       200 0.25
                                       300 0.5
                                       400 0.75
                                       500 1.0}
                           :let [light (mix-colors "#FFFFFF" color weight)]]
                       {:color light
                        :hex (c/as-hex light)
                        :light i})
        dark-colors (for [[i weight] {600 1.0
                                      700 0.8
                                      800 0.6
                                      900 0.4}
                          :let [dark (mix-colors "#000000" color-dark weight)]]
                      {:color dark
                       :hex (c/as-hex dark)
                       :dark i})]
    (concat light-colors
            dark-colors)))

(defn contrast-check [color contrast]
  [:div.flex.items-center.justify-start.gap-1
   [:div.w-2.h-2 {:style {:background-color (c/as-hex color)
                          :border "1px solid #000"}}]
   [:small.font-serif {:style {:color (when (< contrast 4.5)
                                        "#ff0000")}}
    (str (round-double contrast 2) ":1")]])

(defn render-contrast-checks [shade lightest darkest]
  (let [color (:color shade)
        white (c/rgb 255 255 255)
        black (c/rgb 0 0 0)]
    (if (:light shade)
      [:div.flex.flex-col.gap-1
       (contrast-check (:hex darkest) (get-contrast-ratio color (:color darkest)))
       (contrast-check black (get-contrast-ratio color black))]
      [:div.flex.flex-col.gap-1
       (contrast-check (:hex lightest) (get-contrast-ratio (:color lightest) color))
       (contrast-check white (get-contrast-ratio white color))])))

(defn color-testing-component [color]
  (let [theme {:color-primary ["#338593" "#006778"]
               :color-secondary ["#9c3374" "#830051"]
               :color-tertiary ["#7e888d" "#5e6a71"]
               :color-accent ["#33d2c1" "#00c7b2"]
               :color-info ["#339bc9" "#0082bb"]
               :color-link ["#33597f" "#002f5f"]
               :color-success ["#74b939" "#51a808"]
               :color-warning ["#ff7933" "#ff5800"]
               :color-error ["#c73954" "#b90729"]}
        color-shades (apply generate-color-shades (get theme color))
        lightest (first color-shades)
        darkest (last color-shades)]
    [:div
     [:p.lead (str color "-{shade}")]
     [:div.flex.gap-2
      (into [:<>]
            (for [{:keys [light dark hex] :as shade} color-shades]
              [:div.flex.flex-col.gap-1
               [:div.flex.items-center.justify-center.rounded.w-16.h-10
                {:style {:background-color hex
                         :color (if light
                                  (:hex darkest)
                                  (:hex lightest))}}
                [:small.font-sans ; shade number
                 (or light dark)]]
               [:small.font-mono hex]
               (render-contrast-checks shade lightest darkest)]))]]))

^{::clerk/visibility {:result :show}
  ::clerk/width :full}
(clerk/html
 [:div.flex.flex-col.items-center.overflow-hidden
  (color-testing-component :color-primary)
  (color-testing-component :color-secondary)
  (color-testing-component :color-tertiary)
  (color-testing-component :color-success)
  (color-testing-component :color-warning)
  (color-testing-component :color-error)])
