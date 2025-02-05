package dal.graphic.review;

import dal.graphic.Controller;
import dal.word.WordReviewed;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

public class ReviewController extends Controller {
    private List<WordReviewed> words = new ArrayList<>();
    private int wordIndex = 0;

    @FXML
    private TextField nativeTextField;

    @FXML
    private TextField foreignTextField;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private void initialize() {
        previousButton.setDisable(true);
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
        if (wordIndex < words.size()) {
            wordIndex++;
        }

        System.out.println("Next word (" + wordIndex + " of " + words.size() + "): " + words.get(wordIndex));

        updateDisplay();
    }

    private void updateDisplay() {
        WordReviewed currentWord = words.get(wordIndex);

        // Update textFields, depending on which side the user must fill.
        nativeTextField.setDisable(currentWord.isWrittenInForeign());
        foreignTextField.setDisable(!currentWord.isWrittenInForeign());
        if (currentWord.isWrittenInForeign()) {
            nativeTextField.setText(currentWord.getNative_());
            foreignTextField.setText(currentWord.getUserAnswer());
        } else {
            nativeTextField.setText(currentWord.getUserAnswer());
            foreignTextField.setText(currentWord.getForeign());
        }

        // Update buttons
        previousButton.setDisable(wordIndex == 0);
        nextButton.setDisable(wordIndex == words.size() - 1);
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

    public void setWords(List<WordReviewed> words) {
        this.words = words;
        updateDisplay();
    }
}
