package dal.graphic.word.addWord;

import dal.data.db.Db;
import dal.graphic.ConfirmationListener;
import dal.graphic.Controller;
import dal.graphic.ErrorDisplayer;
import dal.graphic.general.SettingsController;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class TranslationController extends Controller {
    private Boolean translationIsUpdating = false; // Flag to avoid making infinite recursion for updates.

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
            if (!translationIsUpdating) { // Ensure that the edit wasn't made by the program.
                // Reset the debounce timer whenever the text changes
                pauseNativeTranslate.setOnFinished(event -> updateTranslate(false));
                pauseNativeTranslate.playFromStart();
            }
        });
        PauseTransition pauseForeignTranslate = new PauseTransition(Duration.millis(750));
        foreignTranslateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!translationIsUpdating) { // Ensure that the edit wasn't made by the program.
                // Reset the debounce timer whenever the text changes
                pauseForeignTranslate.setOnFinished(event -> updateTranslate(true));
                pauseForeignTranslate.playFromStart();
            }
        });

        // Add a Confirmation Listener to each addWord textField.
        nativeTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::addNewWord));
        foreignTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::addNewWord));
        descriptionTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::addNewWord));
    }

    private void updateTranslate(Boolean foreignWasEdited) {
        translationIsUpdating = true; // Avoid infinite loops on translations.

        // Python command to perform the translation using deep_translator package, depending on which side the user typed.
        String command;
        if (!foreignWasEdited) {
            System.out.println("Translating " + nativeTranslateTextField.getText() + " from " + SettingsController.getNativeCode() + " to " + SettingsController.getForeignCode() + "...");
            command = "python -c \"import sys; sys.stdout.reconfigure(encoding='utf-8'); "
                    + "from deep_translator import GoogleTranslator; print(GoogleTranslator(source='"
                    + SettingsController.getNativeCode() + "', target='" + SettingsController.getForeignCode()
                    + "').translate('" + nativeTranslateTextField.getText().replace("'", "\\'") + "'))\"";
        } else {
            System.out.println("Translating " + foreignTranslateTextField.getText() + " from " + SettingsController.getForeignCode() + " to " + SettingsController.getNativeCode() + "...");
            command = "python -c \"import sys; sys.stdout.reconfigure(encoding='utf-8'); "
                    + "from deep_translator import GoogleTranslator; print(GoogleTranslator(source='"
                    + SettingsController.getForeignCode() + "', target='" + SettingsController.getNativeCode()
                    + "').translate('" + foreignTranslateTextField.getText().replace("'", "\\'") + "'))\"";
        }

        try {
            // Run the Python command
            Process process = Runtime.getRuntime().exec(command);

            // Capture and print the python stderr for debugging.
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);
            }

            // Capture the output using UTF-8 encoding (I hate accents and japanese characters).
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            while ((line = reader.readLine()) != null) {
                System.out.println("Translation received: " + line);

                // Update the textField depending on which textField was edited.
                if (!foreignWasEdited) {
                    foreignTranslateTextField.setText(line);
                } else {
                    nativeTranslateTextField.setText(line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        translationIsUpdating = false; // Avoid infinite loops on translations.
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
            ErrorDisplayer.displayError("Please fill both languages for the word.");
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
