package dal.graphic.word.startReview;

import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.graphic.general.SettingsController;
import dal.graphic.word.review.ReviewController;
import dal.data.word.WordReviewed;
import dal.data.word.WordSelector;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class startReviewController extends Controller {
    private int wordCount;
    private ReviewPreference reviewPreference;
    private WriteIn writeIn;
    private int allowedError;

    @FXML
    private GridPane mainGrid;

    @FXML
    private MenuButton wordCountDropdown;

    @FXML
    private MenuButton preferDropdown;

    @FXML
    private MenuButton writeInDropdown;

    @FXML
    private MenuButton allowedErrorDropdown;

    @FXML
    protected void initialize() {
        super.initialize();

        // Retrieve preferred review settings
        wordCount = SettingsController.getDefaultWordCount();
        reviewPreference = SettingsController.getDefaultPreference();
        writeIn = SettingsController.getDefaultWriteIn();
        allowedError = SettingsController.getDefaultAllowedError();

        // Update Dropdowns
        wordCountDropdown.setText(String.valueOf(wordCount));
        preferDropdown.setText(ReviewPreference.getString(reviewPreference));
        writeInDropdown.setText(WriteIn.getString(writeIn));
        allowedErrorDropdown.setText(String.valueOf(allowedError));

        // Add action to all menuItems
        List<MenuButton> allMenuButtons = new ArrayList<>();
        findMenuButtons(mainGrid, allMenuButtons); // Fills the list
        for (MenuButton menuButton: allMenuButtons) {
            for (MenuItem menuItem: menuButton.getItems()) {
                menuItem.setOnAction(event -> this.updateDropdown(event, menuButton));
            }
        }
    }

    private void findMenuButtons(Node node, List<MenuButton> menuButtons) {
        if (node instanceof MenuButton) {
            menuButtons.add((MenuButton) node);
        } else if (node instanceof Parent && !(node instanceof MenuBar)) {
            // Recursively check child nodes
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                findMenuButtons(child, menuButtons);
            }
        }
    }

    private void updateDropdown(ActionEvent event, MenuButton dropdown) {
        // Update MenuButton text
        dropdown.setText(((MenuItem) event.getSource()).getText());

        // Update fields
        wordCount = Integer.parseInt(wordCountDropdown.getText());
        reviewPreference = ReviewPreference.getReviewPreference(preferDropdown.getText());
        writeIn = WriteIn.getWriteIn(writeInDropdown.getText());
        allowedError = Integer.parseInt(allowedErrorDropdown.getText());
    }

    @FXML
    private void addNewWords() {
        SceneManager.switchScene(SceneType.ADD_WORD, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
    }

    @FXML
    public void startReview() {
        System.out.println("Starting review");
        System.out.println("Word count: " + wordCount);
        System.out.println("Review preferance: " + reviewPreference);
        System.out.println("Write in: " + writeIn);
        System.out.println("Allowed error: " + allowedError);

        // Get the words given the parameters
        List<WordReviewed> wordsToReview = WordSelector.getSelection(wordCount, reviewPreference, writeIn);

        // Check that the list exists (i.e. there are enough words in database) before switching scene
        if (wordsToReview == null) {return;}

        // Switch Scene
        SceneManager.switchScene(SceneType.REVIEW, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});

        // Give the words to the new controller
        ReviewController controller = (ReviewController) SceneManager.getCurrentController();
        controller.setWords(wordsToReview, allowedError);
    }
}
