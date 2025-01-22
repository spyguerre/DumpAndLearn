module dal {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;

    opens dal to javafx.fxml;
    exports dal;
    exports dal.graphic.addWord;
    exports dal.graphic.startReview;
    exports dal.word;
    opens dal.graphic.addWord to javafx.fxml;
    opens dal.graphic.startReview;
    exports dal.graphic;
    opens dal.graphic to javafx.fxml;
}
