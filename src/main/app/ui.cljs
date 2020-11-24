(ns app.ui
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.denormalize :as dn]
   [com.fulcrologic.fulcro.algorithms.normalize :as normalize]
   [app.mutations :as api]))

(defsc component [this {:table/keys [id] :as props}]
  {:query [:table/id]
   :ident :table/id}
  (dom/div
    (dom/pre (with-out-str (cljs.pprint/pprint props)))
    ))

(def ui-table (comp/factory component {:keyfn :table/id}))

(defsc Person [this {:person/keys [id name age] :as props} {:keys [onDelete] :as comp-params}]
  {:query         [:person/id :person/name :person/age]
   :ident         :person/id
   :initial-state (fn [{:keys [id name age] :as params}] {:person/id id :person/name name :person/age age})}
  (dom/li :.font-bold.text-xs
          (dom/h5 (str name " (age: " age ")"))
          (dom/button {:onClick #(onDelete id)} "X")))

;; The keyfn generates a react key for each element based on props. See React documentation on keys.
(def ui-person (comp/factory Person {:keyfn :person/name}))

(defsc PersonList [this {:list/keys [id label people input-name input-age] :as props}]
  {:query [:list/id :list/label :list/input-name :list/input-age {:list/people (comp/get-query Person)}]
   :ident :list/id
   :initial-state
   (fn [{:keys [id label] :as params}]
     {:list/id         id
      :list/label      label
      :list/input-name ""
      :list/input-age  ""
      :list/people     (id {:friends [(comp/get-initial-state Person {:id (random-uuid) :name "Sally" :age 32})
                                      (comp/get-initial-state Person {:id (random-uuid) :name "Joe" :age 10})]
                            :enemies [(comp/get-initial-state Person {:id (random-uuid) :name "Fred" :age 2})
                                      (comp/get-initial-state Person {:id (random-uuid) :name "Bob" :age 1})]})})}
  (let [delete-person (fn [person-id] (do (println label "asked to delete" name)
                                          (comp/transact! this [(api/delete-person {:list/id id :person/id person-id})])))]
    (dom/div
      (dom/h4 label)
      (dom/input {:type     "text"
                  :value    input-name
                  :onChange #(m/set-string! this :list/input-name :event %)})
      (dom/input {:type     "text"
                  :value    input-age
                  :onChange #(m/set-string! this :list/input-age :event %)})
      (dom/button
        {:onClick #(merge/merge-component! this Person {:person/id   (random-uuid)
                                                        :person/name input-name
                                                        :person/age  (js/parseInt input-age)}
                                           :append [:list/id id :list/people])}
        "Add person")
      (dom/ul
        (map #(ui-person (comp/computed % {:onDelete delete-person})) people)))))

(def ui-person-list (comp/factory PersonList))

(defsc Todo [this {:todo/keys [id content] :as props}]
  {:query [:todo/id :todo/content]
   :ident :todo/id}
  (dom/div
    (dom/pre (with-out-str (cljs.pprint/pprint props)))
    (dom/p content)))

(def ui-todo (comp/factory Todo {:keyfn :todo/id}))

(defsc TodoList [this {:todo-list/keys [new-item-text todos filter] :as props}]
  {:query         [:todo-list/new-item-text :todo-list/todos :todo-list/filter]
   :ident         (fn [] [:component/by-id :todo-list])
   :initial-state #:todo-list {:new-item-text ""
                               :todos         []
                               :filter        nil}}
  (dom/div
    (dom/h3 "TodoList")
    (map ui-todo todos)
    (dom/button
      {:onClick #(merge/merge-component! this Todo {:todo/id      (random-uuid)
                                                    :todo/content "default"}
                                         :append [:component/by-id :todo-list :todo-list/todos])}
      "Add default todo")
    (dom/pre (with-out-str (cljs.pprint/pprint props)))))

(def ui-todo-list (comp/factory TodoList))

(defsc Root [this {:keys [todo-list]}]
  {:query         [{:todo-list (comp/get-query TodoList)}]
   :initial-state (fn [p] {:todo-list (comp/get-initial-state TodoList {})})}
  (dom/div
    (ui-todo-list todo-list)))

(comment
  (def normalized-state (com.fulcrologic.fulcro.application/current-state app.application/app))
  normalized-state
  (def query (comp/get-query app.ui/Root))
  query
  (def denormalized-state (dn/db->tree query normalized-state normalized-state))
  denormalized-state
  ;; The following demonstrates how the tree of data is auto-normalised.
  ;; The tree->db algorithm simultaneously walks the data tree and a component-annotated query.
  ;; When it reaches a data node whose query metadata names a component with an ident it
  ;; places that data into the appropriate table (by calling your ident function),
  ;; and replaces the data in the tree with its ident.
  (normalize/tree->db app.ui/Root denormalized-state true)
  (dn/db->tree [{:enemies [:list/label]}] (comp/get-initial-state app.ui/Root {}) {})
  (comp/get-initial-state app.ui/Root {})
  (pr-str "hey")
  (+ 3 (+ 1 2)))
