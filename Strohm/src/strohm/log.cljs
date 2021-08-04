(ns strohm.log
  (:require [strohm.impl.log :as impl]))

(defn debug [& args]
  (impl/log (cons :debug args)))

(defn info [& args]
  (impl/log (cons :info args)))

(defn warn [& args]
  (impl/log (cons :warn args)))

(defn error [& args]
  (impl/log (cons :error args)))
