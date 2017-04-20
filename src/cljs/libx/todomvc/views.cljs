(ns libx.todomvc.views
  (:require [libx.core :refer [subscribe then]]
            [reagent.core  :as reagent]))

(defn input [{:keys [value on-change on-key-down on-blur] :as props}]
  [:input
   (merge props
     {:type "text"
      :auto-focus true
      :value value
      :on-change on-change
      :on-key-down on-key-down})])

(defn todo-item []
  (fn [{:keys [db/id todo/title todo/edit todo/done]}]
    (println "Todo item render: title, edit, done" title edit done)
    [:li {:class (str (when done "completed ")
                      (when edit "editing"))}
      [:div.view
        [:input.toggle
          {:type "checkbox"
           :checked (if done true false)
           :on-change #(if done
                         (then :remove [id :todo/done :tag])
                         (then :add [id :todo/done :tag]))}]
        [:label
          {:on-double-click #(then [id :todo/edit-request :action])}
          title]
        [:button.destroy
          {:on-click #(then :remove-entity id)}]]
      (when edit
        [input
          {:class "edit"
           :value edit
           :on-change #(then [id :todo/edit (-> % .-target .-value)])
           :on-key-down #(then [(random-uuid) :input/key-code (.-which %)])
           :on-blur #(then [id :todo/save-edit :action])}])]))

(defn task-list
  []
  (let [{:keys [visible-todos all-complete?]} @(subscribe [:task-list])]
       (prn "All visible in render" visible-todos)
       (prn "All complete?" all-complete?)
      [:section#main
        [:input#toggle-all
          {:type "checkbox"
           :checked (not all-complete?)
           :on-change #(then [(random-uuid) :ui/toggle-complete :tag])}]
        [:label
          {:for "toggle-all"}
          "Mark all as complete"]
        [:ul#todo-list
          (for [todo visible-todos]
            ^{:key (:db/id todo)} [todo-item todo])]]))


(defn footer []
  (let [{:keys [active-count done-count visibility-filter]} @(subscribe [:footer])
        _ (println "[sub] Done count / active count in render" active-count done-count)
        a-fn          (fn [filter-kw txt]
                        [:a {:class (when (= filter-kw visibility-filter) "selected")
                             :href (str "#/" (name filter-kw))} txt])]
    [:footer#footer
     [:span#todo-count
      [:strong active-count] " " (case active-count 1 "item" "items") " left"]
     [:ul#filters
      [:li (a-fn :all    "All")]
      [:li (a-fn :active "Active")]
      [:li (a-fn :done   "Completed")]]
     (when (pos? done-count)
       [:button#clear-completed {:on-click #(then [(random-uuid) :ui/clear-completed :tag])}
        "Clear completed"])]))


(defn task-entry []
  (let [{:keys [db/id new-todo/title]} @(subscribe [:new-todo/title])]
    (prn "New todo title task entry" title)
    [:header#header
      [:h1 "todos"]
      [input
        {:id "new-todo"
         :placeholder "What needs to be done?"
         :value title
         :on-key-down #(then [(random-uuid) :input/key-code (.-which %)])
         :on-change #(then [(random-uuid) :new-todo/title (-> % .-target .-value)])}]]))

(defn todo-app []
  [:div
   [:section#todoapp
    [task-entry]
    (when (seq @(subscribe [:todo-app]))
      [task-list])
    [footer]]
   [:footer#info
    [:p "Double-click to edit a todo"]]])