(ns app.mutations
  (:require
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   ))

(defmutation delete-person
  "Mutation: Delete the person with 'name' from the list with 'list-name'"
  [{list-id   :list/id
    person-id :person/id}]
  (action [{:keys [state]}]
          (let [ident          [:person/id person-id]
                path-to-idents [:list/id list-id :list/people]]
            #_(let [new-list (fn [old-list]
                               (vec (filter #(not= ident %) old-list)))]
                (swap! state update-in path-to-idents new-list))
            ;; OR
            (swap! state merge/remove-ident* ident path-to-idents))
          ))

(comment
  (def old-list [[:person/id 3] [:person/id 4]])
  (vec (filter #(not= [:person/id 4] %) old-list)))

;; (defmutation delete-person
;;   "Mutation: Delete the person with 'name' from the list with 'list-name'"
;;   [{:keys [list-name name]}]
;;   (action [{:keys [state]}]
;;           (let [list-name-to-key {"Friends" :friends
;;                                   "Enemies" :enemies}
;;                 path             [(get list-name-to-key list-name) :list/people]
;;                 old-list         (get-in @state path)
;;                 new-list         (vec (filter #(not= (:person/name %) name) old-list))]
;;             (swap! state assoc-in path new-list))))
