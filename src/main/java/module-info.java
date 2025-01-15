module dal {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;

    opens dal to javafx.fxml;
    exports dal;
    exports dal.graphic.addWord;
    exports dal.word;
    opens dal.graphic.addWord to javafx.fxml;
}