(ns user
  (:require [clojure.tools.namespace.repl :as repl]
            [nextjournal.clerk :as clerk]))

(comment
  (repl/clear)
  (repl/refresh-all)

  (clerk/serve! {:browse? true
                 :watch-paths ["notebooks"]})
  (clerk/serve! {:watch-paths ["notebooks"]})
  (clerk/clear-cache!))
