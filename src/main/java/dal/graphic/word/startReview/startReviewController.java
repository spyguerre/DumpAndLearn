package dal.graphic.word.startReview;

import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.graphic.general.SettingsController;
import dal.graphic.word.review.ReviewController;
import dal.data.word.WordReviewed;
import dal.data.word.WordSelector;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

public class startReviewController extends Controller {
    private int wordCount;
    private ReviewPreference reviewPreference;
    private WriteIn writeIn;

    @FXML
    private MenuButton wordCountDropdown;

    @FXML
    private MenuButton preferDropdown;

    @FXML
    private MenuButton writeInDropdown;

    @FXML
    protected void initialize() {
        super.initialize();

        // Retrieve preferred review settings
        wordCount = SettingsController.getDefaultWordCount();
        reviewPreference = SettingsController.getDefaultPreference();
        writeIn = SettingsController.getDefaultWriteIn();

        // Update Dropdowns
        wordCountDropdown.setText(String.valueOf(wordCount));
        preferDropdown.setText(ReviewPreference.getString(reviewPreference));
        writeInDropdown.setText(WriteIn.getString(writeIn));
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

        reviewPreference = ReviewPreference.getReviewPreference(clickedButton.getText());
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
        System.out.println("Review preferance: " + reviewPreference);
        System.out.println("Write in: " + writeIn);

        // Get the words given the parameters
        List<WordReviewed> wordsToReview = WordSelector.getSelection(wordCount, reviewPreference, writeIn);

        // Check that the list exists (i.e. there are enough words in database) before switching scene
        if (wordsToReview == null) {return;}

        // Switch Scene
        SceneManager.switchScene(SceneType.REVIEW, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});

        // Give the words to the new controller
        ReviewController controller = (ReviewController) SceneManager.getCurrentController();
        controller.setWords(wordsToReview);
    }
}
