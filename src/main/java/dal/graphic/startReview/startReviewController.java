package dal.graphic.startReview;

import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class startReviewController extends Controller {
    @FXML
    private MenuButton wordCountDropdown;

    @FXML
    private MenuButton preferDropdown;

    @FXML
    private void addNewWords() {
        SceneManager.switchScene(SceneType.ADD_WORD, (Stage) root.getScene().getWindow());
    }

    @FXML
    public void updateWordCount(Event event) {
        MenuItem clickedButton = (MenuItem) event.getSource();

        wordCountDropdown.setText(clickedButton.getText());
    }

    @FXML
    public void updatePrefer(Event event) {
        MenuItem clickedButton = (MenuItem) event.getSource();

        preferDropdown.setText(clickedButton.getText());
    }
}
