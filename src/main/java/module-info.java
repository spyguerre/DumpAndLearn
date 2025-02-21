module dal {
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;
    requires org.jsoup;
    requires org.apache.commons.text;
    requires javafx.web;
    requires javafx.media;
    requires javafx.swing;

    exports dal;
    exports dal.graphic;
    exports dal.graphic.addWord;
    exports dal.graphic.startReview;
    exports dal.graphic.review;
    exports dal.graphic.songMenu;
    exports dal.data.db;
    exports dal.data.word;
    exports dal.data.song;

    opens dal to javafx.fxml;
    opens dal.graphic to javafx.fxml;
    opens dal.graphic.addWord to javafx.fxml;
    opens dal.graphic.startReview to javafx.fxml;
    opens dal.graphic.review to javafx.fxml;
    opens dal.graphic.songMenu to javafx.fxml;
    opens dal.data.db to javafx.fxml;
}
