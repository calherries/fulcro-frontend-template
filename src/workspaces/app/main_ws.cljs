(ns app.main-ws
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [nubank.workspaces.card-types.fulcro3 :as f3]
   [nubank.workspaces.core :as ws]))

(defsc FulcroDemo
  [this {:keys [counter] :as props}]
  {:initial-state (fn [_] {:counter 0})
   :ident         (fn [] [::id "singleton"])
   :query         [:counter]}
  (dom/div
    (dom/pre (with-out-str (cljs.pprint/pprint props)))
    (str "Fulcro counter demo [" counter "]")
    (dom/button {:onClick #(m/set-value! this :counter (inc counter))} "+")))

(ws/defcard fulcro-demo-card
  (f3/fulcro-card
    {::f3/root FulcroDemo}))

(defmutation add-thing
  "Mutation: Add a thing to :a-list-of-things"
  [params]
  (action [{:keys [state]}]
          (println (pr-str @state))
          (let [class (comp/registry-key->class :app.client/Thing)]
            (swap! state merge/merge-component class {:a-thing "four stuff"}
                   :append [:a-list-of-things]))))

(defsc Thing [this props]
  {:query [:a-thing :fav-colour]
   :ident :a-thing}
  (dom/div {:style {:border "1px solid black"}}
           (dom/p "This is a thing")
           (dom/pre (with-out-str (cljs.pprint/pprint props)))))

(def ui-thing (comp/factory Thing {:keyfn :a-thing}))

(defsc Root [this props]
  {:query         [{:a-list-of-things (comp/get-query Thing)}] ;; this tells fulcro that Thing owns the state under :an-added-level.
   :initial-state (fn [_] {:a-list-of-things [{:a-thing    "1 stuff"
                                               :fav-colour "red"}
                                              {:a-thing    "2 stuff"
                                               :fav-colour "blue"}]})}
  (dom/div
    (dom/h3 "This is root!")
    (dom/pre (with-out-str (cljs.pprint/pprint props)))
    (map ui-thing (:a-list-of-things props))))

(ws/defcard fulcro-demo-thing
  (f3/fulcro-card
    {::f3/root Root}))

(defonce init (ws/mount))
