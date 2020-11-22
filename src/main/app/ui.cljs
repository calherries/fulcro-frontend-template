(ns app.ui
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]))

(defsc Person [this {:person/keys [name age]}]
  {:initial-state (fn [{:keys [name age] :as params}] {:person/name name :person/age age})}
  (dom/li
    (dom/h5 (str name " (age: " age ")"))))

;; The keyfn generates a react key for each element based on props. See React documentation on keys.
(def ui-person (comp/factory Person {:keyfn :person/name}))

(defsc PersonList [this {:list/keys [label people]}]
  {:initial-state
   (fn [{:keys [label] :as params}]
     {:list/label  label
      :list/people [(comp/get-initial-state Person {:name "Sally" :age 32})
                    (comp/get-initial-state Person {:name "Joe" :age 10})]}
     )}
  (dom/div
    (dom/h4 label)
    (dom/ul
      (map ui-person people))))

(def ui-person-list (comp/factory PersonList))

(defsc Root [this {:keys [friends enemies]}]
  {:initial-state (fn [params]
                    {:friends {:list/label "Friends" :list/people
                               [{:person/name "Sally" :person/age 32}
                                {:person/name "Joe" :person/age 22}]}
                     :enemies {:list/label "Enemies" :list/people
                               [{:person/name "Fred" :person/age 11}
                                {:person/name "Bobby" :person/age 55}]}})}
  (dom/div
    (ui-person-list friends)
    (ui-person-list enemies)))

(comment
  (comp/get-initial-state app.ui/Root {})
  (pr-str "hey")
  (+ 3 (+ 1 2)))
