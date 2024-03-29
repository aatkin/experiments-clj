{:nextjournal.clerk/visibility {:code :hide :result :hide}}

(ns experiments.contrast-ratio
  (:require [clojure.math]
            [garden.color :as c]
            [nextjournal.clerk :as clerk]))

;; # Contrast ratio

;; https://www.accessibility-developer-guide.com/knowledge/colours-and-contrast/how-to-calculate/#the-formula

;; To calculate the contrast ratio, the relative luminance of the lighter colour (L1) is divided through the relative luminance of the darker colour (L2):

;; (L1 + 0.05) / (L2 + 0.05)

;; ## Relative luminance
;; https://www.w3.org/WAI/GL/wiki/Relative_luminance

;; Relative luminance Y follows the photometric definition of luminance L including spectral weighting for human vision.

;; Y (and L) are both linear to changes in the volume of light.
;; Conversions from color spaces where light or lightness are encoded with a power curve, such as most image and video formats,
;; must be linearized before being transformed to Y or the XYZ space.

;; ### sRGB

;; E.g. for sRGB, relative luminance can be calculated from linear RGB components.

(defn rgb->srgb [{:keys [red green blue]}]
  {:red (/ red 255.0)
   :green (/ green 255.0)
   :blue (/ blue 255.0)})

^{:nextjournal.clerk/visibility {:code :show :result :show}}
(c/hex->rgb "#6056EB")

^{:nextjournal.clerk/visibility {:code :show :result :show}}
(-> (c/hex->rgb "#6056EB")
    rgb->srgb)

;; ### Gamma transfer function

;; Then:
;; - if RsRGB <= 0.03928 then R = RsRGB/12.92 else R = ((RsRGB+0.055)/1.055) ^ 2.4
;; - if GsRGB <= 0.03928 then G = GsRGB/12.92 else G = ((GsRGB+0.055)/1.055) ^ 2.4
;; - if BsRGB <= 0.03928 then B = BsRGB/12.92 else B = ((BsRGB+0.055)/1.055) ^ 2.4

(defn curve-fn-1 [d]
  (/ d 12.92))

(defn curve-fn-2 [d]
  (-> d
      (+ 0.055)
      (/ 1.055)
      (clojure.math/pow 2.4)))

(defn gamma-transfer-function [d]
  (let [threshold 0.04045] ; Before May 2021 the value of 0.04045 in the definition was different (0.03928).
    (if (<= d threshold)
      (curve-fn-1 d)
      (curve-fn-2 d))))

^{:nextjournal.clerk/visibility {:code :show :result :show}}
(-> (c/hex->rgb "#6056EB")
    rgb->srgb
    (update :red gamma-transfer-function)
    (update :green gamma-transfer-function)
    (update :blue gamma-transfer-function))

;; ### CIE-Y

;; Now Y can be calculated for these colorspaces by using the coefficients for the Y component of the transform matrix:

;; Y = 0.2126 * R + 0.7152 * G + 0.0722 * B

;; The formula reflects the luminous efficiency function as "green" light is the major component of luminance,
;; responsible for the majority of light perceived by humans, and "blue" light the smallest component.

(defn get-cie-y [srgb]
  (let [expand-multiplier #(comp (partial * %) gamma-transfer-function)]
    (-> srgb
        (update :red (expand-multiplier 0.2126))
        (update :green (expand-multiplier 0.7152))
        (update :blue (expand-multiplier 0.0722)))))

(defn relative-luminance [color]
  (let [{:keys [red green blue]} (get-cie-y (rgb->srgb color))]
    (+ red green blue)))

^{:nextjournal.clerk/visibility {:code :show :result :show}}
(mapv (comp relative-luminance c/hex->rgb)
      ["#FFFFFF" ; white
       "#000000" ; black
       "#6056EB"])

;; ## Contrast ratio

;; Back to the contrast ratio formula:

;; (L1 + 0.05) / (L2 + 0.05)

(defn get-contrast-ratio [color color-dark]
  (let [L1 (relative-luminance color)
        L2 (relative-luminance color-dark)]
    (/ (+ L1 0.05)
       (+ L2 0.05))))

^{:nextjournal.clerk/visibility {:result :show}}
(clerk/table
 {:head ["Light color" "Dark color" "Contrast ratio"]
  :rows (list ["#FFFFFF (white)" "#000000 (black)" (get-contrast-ratio (c/hex->rgb "#FFFFFF") (c/hex->rgb "#000000"))]
              ["#FFFFFF (white)" "#6056EB (primary)" (get-contrast-ratio (c/hex->rgb "#FFFFFF") (c/hex->rgb "#6056EB"))]
              ["#6056EB (primary)" "#000000 (black)" (get-contrast-ratio (c/hex->rgb "#6056EB") (c/hex->rgb "#000000"))])})
