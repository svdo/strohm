(ns strohm.impl.store)

(def default-initial-state {})

(defn create-store 
  [reducer & {:keys [initial-state]}]
  {:state (or initial-state default-initial-state)
   :reducer reducer})

(defn- identity-reducer [state _] state)

(defn get-reducer-fn [reducer action-type]
  (if (associative? reducer)
    (if-let [reducer-for-action (get reducer action-type)]
      (fn reducer-from-map [state action]
        (reducer-for-action state (:payload action)))
      identity-reducer)
    reducer))

(defn reduce-action [action store] 
  (let [reducer     (:reducer store)
        reducing-fn (get-reducer-fn reducer (:type action))]
    (update store
            :state
            (fn [state] (reducing-fn state action)))))

(defn- apply-substate-reducer
  [action state substate-key reducer]
  (let [reducer-fn (get-reducer-fn reducer (:type action))]
    (update state
            substate-key
            (fn [substate] (reducer-fn substate action)))))

(defn combine-reducers [reducers]
  (fn combined-reducer [state action]
    (reduce-kv (partial apply-substate-reducer action)
               state
               reducers)))

(defn  state-for-prop-spec [state [prop-name prop-spec]]
  [prop-name
   (reduce (fn [acc prop] (get acc prop)) state prop-spec)])

(defn state->props [state props-spec]
  (into {} (map (partial state-for-prop-spec state) props-spec)))

(comment
  ;;
  ;; USING STORE AS ATOM
  ;;
  (let [store (atom nil)
        reduce-action' (fn [s action]
                         (let [reducer     (:reducer s)
                               reducing-fn (get-reducer-fn reducer (:type action))
                               updated-s (update s
                                                 :state
                                                 (fn [state] (tap> state) (reducing-fn state action)))]
                           (tap> updated-s)
                           updated-s))
        create-store' (fn [reducer & {:keys [initial-state middlewares]}]
                        (let [base-dispatch (fn base-dispatch [action] (swap! store reduce-action' action))]
                          {:state (or initial-state default-initial-state)
                           :reducer reducer
                           :dispatch ((apply comp (map (fn [m] (m store)) (reverse middlewares))) base-dispatch)}))
        reducer (fn [state action]
                  (update state :received-actions #(conj % action)))
        logger-middleware (fn [store]
                            (fn logger-middleware [next]
                              (fn logger-dispatcher [action]
                                (prn "state before:" (:state @store))
                                (prn "dispatch action:" action)
                                (next action)
                                (prn "state after:" (:state @store))
                                (prn "------------------------"))))
        action-duplicating-middleware (fn [_store]
                                        (fn action-duplicating-middleware [next]
                                          (fn action-duplicating-dispatcher [action]
                                            (next action)
                                            (next action))))
        dispatch          (fn [a] ((:dispatch @store) a))]
    (reset! store (create-store' reducer
                                 :initial-state {:received-actions []}
                                 :middlewares [logger-middleware action-duplicating-middleware]))
    (dispatch {:type "first action"})
    (dispatch {:type "second action"})
    @store))
