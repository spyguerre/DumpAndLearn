package dal.graphic.word.addWord;

import dal.graphic.Controller;
import dal.graphic.general.SettingsController;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class TranslationController extends Controller {
    private Boolean translationIsUpdating = false; // Flag to avoid making infinite recursion for updates.

    @FXML
    protected TextField nativeTextField;

    @FXML
    protected TextField foreignTextField;

    @FXML
    protected TextField descriptionTextField;

    @FXML
    private TextField nativeTranslateTextField;

    @FXML
    private TextField foreignTranslateTextField;

    @FXML
    protected void initialize() {
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
    }

    private void updateTranslate(Boolean foreignWasEdited) {
        translationIsUpdating = true; // Avoid infinite loops on translations.

        // Python command to perform the translation using deep_translator package, depending on which side the user typed.
        String command;
        if (!foreignWasEdited) {
            System.out.println("Translating " + nativeTranslateTextField.getText() + " from " + SettingsController.getNativeCode() + " to " + SettingsController.getForeignCode() + "...");
            command = "python -c \"from deep_translator import GoogleTranslator; print(GoogleTranslator(source='"
                    + SettingsController.getNativeCode() + "', target='" + SettingsController.getForeignCode()
                    + "').translate('" + nativeTranslateTextField.getText() + "'))\"";
        } else {
            System.out.println("Translating " + foreignTranslateTextField.getText() + " from " + SettingsController.getForeignCode() + " to " + SettingsController.getNativeCode() + "...");
            command = "python -c \"from deep_translator import GoogleTranslator; print(GoogleTranslator(source='"
                    + SettingsController.getForeignCode() + "', target='" + SettingsController.getNativeCode()
                    + "').translate('" + foreignTranslateTextField.getText() + "'))\"";
        }

        try {
            // Run the Python command
            Process process = Runtime.getRuntime().exec(command);

            // Capture the output from the process (translation)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Translation recieved: " + line);

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
}
