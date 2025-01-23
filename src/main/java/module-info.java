module dal {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;

    exports dal;
    exports dal.graphic;
    exports dal.graphic.addWord;
    exports dal.graphic.startReview;
    exports dal.graphic.review;
    exports dal.word;

    opens dal to javafx.fxml;
    opens dal.graphic to javafx.fxml;
    opens dal.graphic.addWord to javafx.fxml;
    opens dal.graphic.startReview to javafx.fxml;
    opens dal.graphic.review to javafx.fxml;
}
