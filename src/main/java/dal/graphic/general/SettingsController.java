package dal.graphic.general;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dal.graphic.Controller;
import dal.graphic.ErrorDisplayer;
import dal.graphic.Languages;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsController extends Controller {

    @FXML
    private MenuButton foreignDropDown;

    @FXML
    private MenuButton nativeDropDown;

    @FXML
    private void initialize() {
        // Initialize the default settings if necessary.
        File settingsFile = new File("settings.json");
        if (!settingsFile.exists()) {
            initDefaultSettings();
        }

        // Read JSON file and convert it to a Map<String, String>
        Map<String, String> settings = getSettings();
        assert settings != null;

        // Update the dropdowns' text to match known settings.
        nativeDropDown.setText(settings.get("native"));
        foreignDropDown.setText(settings.get("foreign"));
    }

    private void initDefaultSettings() {
        // Create a dictionary with the default settings.
        Map<String, String> settings = new HashMap<>();
        settings.put("native", "French");
        settings.put("foreign", "English");

        // Convert to JSON and save to a file.
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("settings.json"), settings);
            System.out.println("Settings saved initialized in settings.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> getSettings() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Read JSON file and convert it to a Map<String, String>
            Map<String, String> settings = objectMapper.readValue(
                    new File("settings.json"), new TypeReference<>() {}
            );

            System.out.println("Successfully loaded settings from settings.json");
            return settings;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void updateNative(ActionEvent event) {
        nativeDropDown.setText(((MenuItem) event.getSource()).getText());
    }

    @FXML
    private void updateForeign(ActionEvent event) {
        foreignDropDown.setText(((MenuItem) event.getSource()).getText());
    }

    @FXML
    private void cancel() {
        mainMenu();
    }

    @FXML
    private void save() {
        // Ensure the user has selected a language for both sides.
        if (nativeDropDown.getText().equals("Choose language")) {
            ErrorDisplayer.displayError("Please select a native language.");
            return;
        }
        if (foreignDropDown.getText().equals("Choose language")) {
            ErrorDisplayer.displayError("Please select a foreign language.");
            return;
        }
        // Ensure the two languages are different.
        if (nativeDropDown.getText().equals(foreignDropDown.getText())) {
            ErrorDisplayer.displayError("The native language and foreign language can't be the same.");
            return;
        }

        // Create a dictionary with the settings.
        Map<String, String> settings = new HashMap<>();
        settings.put("native", nativeDropDown.getText());
        settings.put("foreign", foreignDropDown.getText());

        // Convert to JSON and save to a file.
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("settings.json"), settings);
            System.out.println("Settings saved successfully in settings.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Back to Main Menu.
        mainMenu();
    }

    public static String getNativeCode() {
        Map<String, String> settings = getSettings();
        assert settings != null;
        return Languages.getCode(Languages.valueOf(settings.get("native").toUpperCase()));
    }

    public static String getForeignCode() {
        Map<String, String> settings = getSettings();
        assert settings != null;
        return Languages.getCode(Languages.valueOf(settings.get("foreign").toUpperCase()));
    }
}
