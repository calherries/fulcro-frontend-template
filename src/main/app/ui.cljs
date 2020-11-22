(ns app.ui
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]))

(defsc Person [this {:person/keys [name age]} {:keys [onDelete] :as comp-params}]
  {:query         [:person/name :person/age]
   :initial-state (fn [{:keys [name age] :as params}] {:person/name name :person/age age})}
  (dom/li
    (dom/h5 (str name " (age: " age ")"))
    (dom/button {:onClick #(onDelete name)} "X")))

;; The keyfn generates a react key for each element based on props. See React documentation on keys.
(def ui-person (comp/factory Person {:keyfn :person/name}))

(defsc PersonList [this {:list/keys [label people]}]
  {:query [:list/label {:list/people (comp/get-query Person)}]
   :initial-state
   (fn [{:keys [label] :as params}]
     {:list/label  label
      :list/people [(comp/get-initial-state Person {:name "Sally" :age 32})
                    (comp/get-initial-state Person {:name "Joe" :age 10})]})}
  (let [delete-person (fn [name] (println label "asked to delete" name))]
    (dom/div
      (dom/h4 label)
      (dom/ul
        (map #(ui-person (comp/computed % {:onDelete delete-person})) people)))))

(def ui-person-list (comp/factory PersonList))

(defsc Root [this {:keys [friends enemies]}]
  {:query         [{:friends (comp/get-query PersonList)}
                   {:enemies (comp/get-query PersonList)}]
   :initial-state (fn [params] {:friends (comp/get-initial-state PersonList {:label "Friends"})
                                :enemies (comp/get-initial-state PersonList {:label "Enemies"})})}
  (dom/div
    (ui-person-list friends)
    (ui-person-list enemies)))

(comment
  (fdn/db->tree [{:enemies [:list/label]}] (comp/get-initial-state app.ui/Root {}) {})
  (comp/get-initial-state app.ui/Root {})
  (pr-str "hey")
  (+ 3 (+ 1 2)))
