package dal.graphic.general;

import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainMenuController extends Controller {
    @FXML
    private void addNewWord() {
        SceneManager.switchScene(SceneType.ADD_WORD, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
    }

    @FXML
    private void startReviewSession() {
        SceneManager.switchScene(SceneType.START_REVIEW, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
    }

    @FXML
    private void learnFromSong() {
        SceneManager.switchScene(SceneType.SONG_MENU, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
    }

    @FXML
    private void listenToAPodcast() {
        SceneManager.switchScene(SceneType.PODCAST_MENU, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
    }

    @FXML
    private void scanSomeText() {
        SceneManager.switchScene(SceneType.START_OCR, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
    }
}
