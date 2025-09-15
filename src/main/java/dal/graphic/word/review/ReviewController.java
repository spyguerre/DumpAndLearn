package dal.graphic.word.review;

import dal.graphic.KeyboardListener;
import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.data.word.WordReviewed;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
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
    private HBox hintHBox;

    @FXML
    private Text hintText;

    @FXML
    protected void initialize() {
        super.initialize();

        previousButton.setDisable(true);

        // Add listeners for enter
        nativeTextField.addEventHandler(KeyEvent.KEY_PRESSED, new KeyboardListener(this::next));
        foreignTextField.addEventHandler(KeyEvent.KEY_PRESSED, new KeyboardListener(this::next));

        // Add listeners for shift enter
        nativeTextField.addEventHandler(KeyEvent.KEY_PRESSED, new KeyboardListener(this::previous, KeyCode.ENTER, true, false, false));
        foreignTextField.addEventHandler(KeyEvent.KEY_PRESSED, new KeyboardListener(this::previous, KeyCode.ENTER, true, false, false));

        // Add listener for ctrl + h
        nativeTextField.addEventHandler(KeyEvent.KEY_PRESSED, new KeyboardListener(this::revealHint, KeyCode.H, false, true, false));
        foreignTextField.addEventHandler(KeyEvent.KEY_PRESSED, new KeyboardListener(this::revealHint, KeyCode.H, false, true, false));
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

        if (wordIndex < words.size()-1) {
            // Update index
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

    @FXML
    private void onKeyTyped() {
        saveUserAnswer();
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
            foreignTextField.positionCaret(foreignTextField.getText().length()); // Move caret to the end
        } else {
            nativeTextField.setText(currentWord.getUserAnswer());
            foreignTextField.setText(currentWord.getForeign());
            nativeTextField.requestFocus();
            nativeTextField.positionCaret(nativeTextField.getText().length()); // Move caret to the end
        }

        // Update hint
        // Show hint if hintRevealed is > 0, else hide it.
        System.out.println(currentWord.getHintRevealed());
        if (currentWord.getHintRevealed() == 0) {
            hideHint();
        } else {
            int hintRevealed = words.get(wordIndex).getHintRevealed();
            String answer = words.get(wordIndex).isWrittenInForeign() ? words.get(wordIndex).getForeign() : words.get(wordIndex).getNative_();
            showHint();
            // Set the hint text to the first hintRevealed characters of the correct answer, then "_"s for the rest of the word's length.
            hintText.setText(answer.substring(0, Math.min(hintRevealed, answer.length())) + ".".repeat(Math.max(0, answer.length() - hintRevealed)));
            showHint();
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
    }

    public List<WordReviewed> getWords() {
        return words;
    }

    public void setWords(List<WordReviewed> words, int allowedError) {
        this.allowedError = allowedError;
        this.words = words;
        updateDisplay();
    }

    @FXML
    private void revealHint() {
        System.out.println("Hint revealed.");
        words.get(wordIndex).incrHintRevealed();
        updateDisplay();
    }

    private void showHint() {
        // Checks if the hintButton is already in the hintHBox, else add it to its children.
        if (!hintHBox.getChildren().contains(hintText)) {
            hintHBox.getChildren().add(hintText);
        }
    }

    private void hideHint() {
        // Removes the hintButton if it is in the hintHBox' children.
        hintHBox.getChildren().remove(hintText);
    }
}
