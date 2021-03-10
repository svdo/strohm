(ns app.entries.reducer
  (:require [strohm.native :refer [create-reducer]]))

(defn- update-entry [entries payload]
  (if (get entries (:entry/id payload))
    (update entries (:entry/id payload) (fn [entry] (merge entry payload)))
    entries))

(def reducer
  (create-reducer {"add-entry" #(assoc %1 (:entry/id %2) %2)
                   "update-entry" update-entry
                   "remove-entry" #(dissoc %1 (:entry/id %2))}))

(def initial-state
  {1 {:entry/id 1
      :entry/title "Title 1"
      :entry/text "Text 1"
      :entry/created (double (- (.getTime (js/Date.)) 60000))}
   2 {:entry/id 2
      :entry/title "Title 2"
      :entry/text "Text 2"
      :entry/created (double (- (.getTime (js/Date.)) 10000))}
   3 {:entry/id 3
      :entry/title "Title 3"
      :entry/text "Text 3"
      :entry/created (double (.getTime (js/Date.)))}
   4 {:entry/id 4
      :entry/title "Lorem Ipsum"
      :entry/text "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
      :entry/created (double (.getTime (js/Date.)))}})
