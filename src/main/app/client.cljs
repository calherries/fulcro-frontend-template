(ns app.client
  (:require
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]))

(defonce app (app/fulcro-app))

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
           (pr-str props)))

(def ui-thing (comp/factory Thing {:keyfn :a-thing}))

(defsc Root [this props]
  {:query         [{:a-list-of-things (comp/get-query Thing)}] ;; this tells fulcro that Thing owns the state under :an-added-level.
   :initial-state (fn [_] {:a-list-of-things [{:a-thing    "1 stuff"
                                               :fav-colour "red"}
                                              {:a-thing    "2 stuff"
                                               :fav-colour "blue"}]})}
  (dom/div
    (dom/h3 "This is root!")
    (pr-str props)
    (map ui-thing (:a-list-of-things props))))

(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function. See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/mount! app Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source. See shadow-cljs.edn"
  []
  ;; re-mounting will cause forced UI refresh, update internals, etc.
  (app/mount! app Root "app")
  ;; As of Fulcro 3.3.0, this addition will help with stale queries when using dynamic routing:
  (comp/refresh-dynamic-queries! app)
  (js/console.log "Hot reload"))

(comment
  (pr-str "hey")
  (+ 3 (+ 1 2))
  (comp/transact! app [(add-thing {})])
)
