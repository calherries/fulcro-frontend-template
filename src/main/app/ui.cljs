(ns app.ui
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.denormalize :as dn]
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
  (dom/li
    (dom/h5 (str name " (age: " age ")"))
    (dom/button {:onClick #(onDelete name)} "X")))

;; The keyfn generates a react key for each element based on props. See React documentation on keys.
(def ui-person (comp/factory Person {:keyfn :person/name}))

(defsc PersonList [this {:list/keys [id label people] :as props}]
  {:query [:list/id :list/label {:list/people (comp/get-query Person)}]
   :ident :list/id
   :initial-state
   (fn [{:keys [id label] :as params}]
     {:list/id     id
      :list/label  label
      :list/people (id {:friends [(comp/get-initial-state Person {:id 1 :name "Sally" :age 32})
                                  (comp/get-initial-state Person {:id 2 :name "Joe" :age 10})]
                        :enemies [(comp/get-initial-state Person {:id 3 :name "Fred" :age 2})
                                  (comp/get-initial-state Person {:id 4 :name "Bob" :age 1})]})})}
  (let [delete-person (fn [name] (do (println label "asked to delete" name)
                                     (comp/transact! this [(api/delete-person {:list-name label :name name})])))]
    (dom/div
      (dom/h4 label)
      (dom/ul
        (map #(ui-person (comp/computed % {:onDelete delete-person})) people)))))

(def ui-person-list (comp/factory PersonList))

(defsc Root [this {:keys [friends enemies]}]
  {:query         [{:friends (comp/get-query PersonList)}
                   {:enemies (comp/get-query PersonList)}]
   :initial-state (fn [params] {:friends (comp/get-initial-state PersonList {:id    :friends
                                                                             :label "Friends"})
                                :enemies (comp/get-initial-state PersonList {:id    :enemies
                                                                             :label "Enemies"})})}
  (dom/div
    (ui-person-list friends)
    (ui-person-list enemies)))

(comment
  (def state (com.fulcrologic.fulcro.application/current-state app.application/app))
  state
  query
  (def query (comp/get-query app.ui/Root))
  (dn/db->tree query state state)
  (dn/db->tree [{:enemies [:list/label]}] (comp/get-initial-state app.ui/Root {}) {})
  (comp/get-initial-state app.ui/Root {})
  (pr-str "hey")
  (+ 3 (+ 1 2)))
