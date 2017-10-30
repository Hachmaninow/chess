(ns chessdojo.dialogs.game-info-editor-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [chessdojo.dialogs.game-info-editor :refer [game-info->str str->game-info]]))

(deftest test-game-info->str
  (testing "when game info is converted to string then every kv-pair is a separate line"
    (is (= "White=Anand\nBlack=Kramnik\nTitle=Great game"
          (game-info->str {"White" "Anand" "Black" "Kramnik" "Title" "Great game"})))))

(deftest test-str->game-info
  (testing "when a string is converted to game info then every line is translated to one kv-pair"
    (is (= {"White" "Anand" "Black" "Kramnik" "Title" "Great game"}
          (str->game-info "White=Anand\nBlack=Kramnik\nTitle=Great game")))))