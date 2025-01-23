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
    private WriteIn writeIn;

    @FXML
    private MenuButton wordCountDropdown;

    @FXML
    private MenuButton preferDropdown;

    @FXML
    private MenuButton writeInDropdown;

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
    private void updateWordCount(Event event) {
        MenuItem clickedButton = (MenuItem) event.getSource();

        wordCountDropdown.setText(clickedButton.getText());

        wordCount = Integer.parseInt(clickedButton.getText());
    }

    @FXML
    private void updatePrefer(Event event) {
        MenuItem clickedButton = (MenuItem) event.getSource();

        preferDropdown.setText(clickedButton.getText());

        reviewPreferance = ReviewPreference.getReviewPreference(clickedButton.getText());
    }

    @FXML
    private void updateWriteIn(Event event) {
        MenuItem clickedButton = (MenuItem) event.getSource();

        writeInDropdown.setText(clickedButton.getText());

        writeIn = WriteIn.getWriteIn(clickedButton.getText());
    }

    @FXML
    public void startReview() {
        System.out.println("Starting review");
        System.out.println("Word count: " + wordCount);
        System.out.println("Review preferance: " + reviewPreferance);
        System.out.println("Write in: " + writeIn);
    }
}
