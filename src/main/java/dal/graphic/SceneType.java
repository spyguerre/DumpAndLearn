package dal.graphic;

import javafx.fxml.FXMLLoader;

public enum SceneType {
    ADD_WORD,
    START_REVIEW,
    REVIEW,
    CORRECTION;

    public static FXMLLoader getSceneFxml(SceneType scene) {
        String fp = "";
        switch (scene) {
            case ADD_WORD -> fp = "addWord/addWord.fxml";
            case START_REVIEW -> fp = "startReview/startReview.fxml";
            case REVIEW -> fp = "review/review.fxml";
            case CORRECTION -> fp = "review/correction.fxml";
        }

        return new FXMLLoader(SceneType.class.getResource(fp));
    }
}
