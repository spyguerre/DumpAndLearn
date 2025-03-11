module dal {
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;
    requires org.jsoup;
    requires org.apache.commons.text;
    requires javafx.web;
    requires javafx.media;
    requires javafx.swing;
    requires com.fasterxml.jackson.databind;
    requires tess4j;

    exports dal;
    exports dal.graphic;
    exports dal.graphic.general;
    exports dal.graphic.word.addWord;
    exports dal.graphic.word.startReview;
    exports dal.graphic.word.review;
    exports dal.graphic.song.menu;
    exports dal.graphic.song.playing;
    exports dal.data.db;
    exports dal.data.word;
    exports dal.data.song;
    exports dal.data.OCR;

    opens dal to javafx.fxml;
    opens dal.graphic to javafx.fxml;
    opens dal.graphic.word.addWord to javafx.fxml;
    opens dal.graphic.word.startReview to javafx.fxml;
    opens dal.graphic.word.review to javafx.fxml;
    opens dal.graphic.song.menu to javafx.fxml;
    opens dal.graphic.song.playing to javafx.fxml;
    opens dal.graphic.general to javafx.fxml;
}
