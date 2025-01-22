package dal.graphic;

import javafx.fxml.FXMLLoader;

public enum SceneType {
    ADD_WORD,
    START_REVIEW;

    public static FXMLLoader getSceneFxml(SceneType scene) {
        String fp = "";
        switch (scene) {
            case ADD_WORD -> fp = "addWord/addWord.fxml";
            case START_REVIEW -> fp = "startReview/startReview.fxml";
        }

        return new FXMLLoader(SceneType.class.getResource(fp));
    }
}
