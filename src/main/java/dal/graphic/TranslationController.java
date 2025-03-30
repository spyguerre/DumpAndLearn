package dal.graphic;

import dal.data.db.Db;
import dal.data.translation.Translator;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TranslationController extends Controller {
    // Flag to avoid making infinite recursion for updates.
    private final AtomicBoolean translationIsUpdating = new AtomicBoolean(false);

    @FXML
    protected TextField nativeTextField;

    @FXML
    protected TextField foreignTextField;

    @FXML
    protected TextField descriptionTextField;

    @FXML
    protected TextField nativeTranslateTextField;

    @FXML
    protected TextField foreignTranslateTextField;

    @FXML
    protected void initialize() {
        super.initialize();

        // Set listener to update the two translateTextFields
        PauseTransition pauseNativeTranslate = new PauseTransition(Duration.millis(750));
        nativeTranslateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Ensure that the edit wasn't made by the program.
            if (translationIsUpdating.get()) {
                return; // Another translation is still in progress
            }
            // Reset the debounce timer whenever the text changes
            pauseNativeTranslate.setOnFinished(event -> updateTranslate(false));
            pauseNativeTranslate.playFromStart();
        });
        PauseTransition pauseForeignTranslate = new PauseTransition(Duration.millis(750));
        foreignTranslateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Ensure that the edit wasn't made by the program.
            if (translationIsUpdating.get()) {
                return; // Another translation is still in progress
            }
            // Reset the debounce timer whenever the text changes
            pauseForeignTranslate.setOnFinished(event -> updateTranslate(true));
            pauseForeignTranslate.playFromStart();
        });

        // Add a Confirmation Listener to each addWord textField.
        nativeTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::addNewWord));
        foreignTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::addNewWord));
        descriptionTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::addNewWord));
    }

    private void updateTranslate(Boolean foreignWasEdited) {
        new Thread(() -> {
            translationIsUpdating.set(true); // Avoid infinite loops on translations.
            // Perform the translation.
            String translation = Translator.translate(
                    foreignWasEdited ? foreignTranslateTextField.getText() : nativeTranslateTextField.getText(),
                    foreignWasEdited
            );

            // Update the textField depending on which textField was edited.
            Platform.runLater(() -> {
                (foreignWasEdited ? nativeTranslateTextField : foreignTranslateTextField).setText(translation);
                translationIsUpdating.set(false); // Reset flag only after updating UI.
            });
        }).start();
}

    @FXML
    private void transferTranslationToAddWordFields() {
        // Copy the textFields values from translation to addWord so user can more easily edit them.
        nativeTextField.setText(nativeTranslateTextField.getText());
        foreignTextField.setText(foreignTranslateTextField.getText());
    }

    @FXML
    public void addNewWord() {
        System.out.println("Adding new word...");

        // Ensure that there is a word in both textFields
        if (nativeTextField.getText().isEmpty() || foreignTextField.getText().isEmpty()) {
            System.out.println("Couldn't find native or foreign word.");
            NotificationDisplayer.displayError("Please fill both languages for the word.");
            return;
        }

        String description = null;
        if (!descriptionTextField.getText().isEmpty()) {
            description = descriptionTextField.getText();
        }

        Db.insertNewWord(nativeTextField.getText(), foreignTextField.getText(), description);
        resetInputTextFields();
    }

    private void resetInputTextFields() {
        nativeTextField.clear();
        foreignTextField.clear();
        descriptionTextField.clear();

        nativeTextField.requestFocus();
    }
}
