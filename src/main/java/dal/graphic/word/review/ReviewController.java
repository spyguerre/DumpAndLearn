package dal.graphic.word.review;

import dal.graphic.ConfirmationListener;
import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.data.word.WordReviewed;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ReviewController extends Controller {
    private List<WordReviewed> words = new ArrayList<>();
    private int wordIndex = 0;
    private int allowedError;

    @FXML
    private TextField nativeTextField;

    @FXML
    private TextField foreignTextField;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    protected void initialize() {
        super.initialize();

        previousButton.setDisable(true);

        nativeTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::next));
        foreignTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::next));
    }

    @FXML
    private void previous() {
        saveUserAnswer();

        // Update index
        if (wordIndex > 0) {
            wordIndex--;
        }

        System.out.println("Previous word (" + wordIndex + " of " + words.size() + "): " + words.get(wordIndex));

        updateDisplay();
    }

    @FXML
    private void next() {
        saveUserAnswer();

        // Update index
        if (wordIndex < words.size()-1) {
            wordIndex++;
        } else { // "Finish" clicked instead of "next": Correct words in new scene
            // Switch Scene
            SceneManager.switchScene(SceneType.CORRECTION, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});

            // Pass the words to the new Controller
            CorrectionController newController = (CorrectionController) SceneManager.getCurrentController();
            newController.setWords(words, allowedError);
        }

        System.out.println("Next word (" + wordIndex + " of " + words.size() + "): " + words.get(wordIndex));

        updateDisplay();
    }

    private void updateDisplay() {
        WordReviewed currentWord = words.get(wordIndex);

        // Update textFields and request focus, depending on which side the user must fill.
        nativeTextField.setDisable(currentWord.isWrittenInForeign());
        foreignTextField.setDisable(!currentWord.isWrittenInForeign());
        if (currentWord.isWrittenInForeign()) {
            nativeTextField.setText(currentWord.getNative_());
            foreignTextField.setText(currentWord.getUserAnswer());
            foreignTextField.requestFocus();
        } else {
            nativeTextField.setText(currentWord.getUserAnswer());
            foreignTextField.setText(currentWord.getForeign());
            nativeTextField.requestFocus();
        }

        // Update buttons
        previousButton.setDisable(wordIndex == 0);
        nextButton.setText(wordIndex == words.size() - 1 ? "Finish" : "Next");
    }

    private void saveUserAnswer() {
        WordReviewed currentWord = words.get(wordIndex);
        if (currentWord.isWrittenInForeign()) {
            currentWord.setUserAnswer(foreignTextField.getText());
        } else {
            currentWord.setUserAnswer(nativeTextField.getText());
        }

        System.out.println("Saved user answer");
    }

    public List<WordReviewed> getWords() {
        return words;
    }

    public void setWords(List<WordReviewed> words, int allowedError) {
        this.allowedError = allowedError;
        this.words = words;
        updateDisplay();
    }
}
