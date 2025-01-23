package dal.graphic.startReview;

import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class startReviewController extends Controller {
    private int wordCount;
    private ReviewPreference reviewPreferance;

    @FXML
    private MenuButton wordCountDropdown;

    @FXML
    private MenuButton preferDropdown;

    @FXML
    private void initialize() {
        wordCount = Integer.parseInt(wordCountDropdown.getText());
        reviewPreferance = ReviewPreference.getReviewPreference(preferDropdown.getText());
    }

    @FXML
    private void addNewWords() {
        SceneManager.switchScene(SceneType.ADD_WORD, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
    }

    @FXML
    public void updateWordCount(Event event) {
        MenuItem clickedButton = (MenuItem) event.getSource();

        wordCountDropdown.setText(clickedButton.getText());

        wordCount = Integer.parseInt(wordCountDropdown.getText());
    }

    @FXML
    public void updatePrefer(Event event) {
        MenuItem clickedButton = (MenuItem) event.getSource();

        preferDropdown.setText(clickedButton.getText());

        reviewPreferance = ReviewPreference.getReviewPreference(preferDropdown.getText());
    }

    @FXML
    public void startReview() {
        System.out.println("Starting review");
        System.out.println("Word count: " + wordCount);
        System.out.println("Review preferance: " + reviewPreferance);
    }
}
