package dal.graphic;

import javafx.fxml.FXMLLoader;

public enum SceneType {
    MAIN_MENU,
    SETTINGS,
    ADD_WORD,
    START_REVIEW,
    REVIEW,
    CORRECTION,
    SONG_MENU,
    SONG_PLAYING,
    TRANSLATION_POPUP;

    public static FXMLLoader getSceneFxml(SceneType scene) {
        String fp = "";
        switch (scene) {
            case MAIN_MENU -> fp = "mainMenu.fxml";
            case SETTINGS -> fp = "settings.fxml";
            case ADD_WORD -> fp = "word/addWord/addWord.fxml";
            case START_REVIEW -> fp = "word/startReview/startReview.fxml";
            case REVIEW -> fp = "word/review/review.fxml";
            case CORRECTION -> fp = "word/review/correction.fxml";
            case SONG_MENU -> fp = "song/menu/songMenu.fxml";
            case SONG_PLAYING -> fp = "song/playing/playing.fxml";
            case TRANSLATION_POPUP -> fp = "song/playing/popup.fxml";
        }

        return new FXMLLoader(SceneType.class.getResource(fp));
    }
}
