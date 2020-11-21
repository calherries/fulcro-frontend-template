(ns app.client
  (:require
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]))

(defonce app (app/fulcro-app))

(defsc ItemList [this props]
  {:query         [{:item-list/all-items []}]
   :initial-state {:item-list/all-items []}
   :ident         (fn [] [:component/id ::item-list])}
  (dom/p
    (pr-str "...")))

(def ui-item-list (comp/factory ItemList))

(defsc Thing [this props]
  {:query         [:a-thing]
   :initial-state {:a-thing "Some stuff!"}}
  (dom/div {:style {:border "1px solid black"}}
           (dom/p "This is a thing")
           (pr-str props)))

(def ui-thing (comp/factory Thing #_{:keyfn :thing/id}))

(defsc Root [this props]
  {:query         [{:an-added-level (comp/get-query Thing)}] ;; this tells fulcro that Thing owns the state under :an-added-level.
   :initial-state (fn [_] {:an-added-level (comp/get-initial-state Thing)})} ;; So we need to use the function type of initial-state.
  (dom/div
    (dom/h3 "This is root!")
    (pr-str props)
    (ui-thing (:an-added-level props))))

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
