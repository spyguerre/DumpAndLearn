package dal.graphic.general;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sarxos.webcam.Webcam;
import dal.graphic.Controller;
import dal.graphic.ErrorDisplayer;
import dal.data.Languages;
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
    private MenuButton captureDeviceDropdown;

    @FXML
    protected void initialize() {
        super.initialize();

        // Initialize the default settings if necessary.
        File settingsFile = new File("settings.json");
        if (!settingsFile.exists()) {
            initDefaultSettings();
        }

        // Update the dropdowns' text to match known settings.
        nativeDropDown.setText(getNativeLanguage());
        foreignDropDown.setText(getForeignLanguage());
        captureDeviceDropdown.setText(getCaptureDevice());

        // Update capture device options
        for (Webcam webcam: Webcam.getWebcams()) {
            // Create and add menuItem
            MenuItem item = new MenuItem(webcam.getName());
            item.setOnAction(this::updateCaptureDevice);
            captureDeviceDropdown.getItems().add(item);
        }
    }

    private static void initDefaultSettings() {
        // Create a dictionary with the default settings.
        Map<String, String> settings = new HashMap<>();
        settings.put("native", "French");
        settings.put("foreign", "English");
        settings.put("captureDevice", "None");

        // Convert to JSON and save to a file.
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("settings.json"), settings);
            System.out.println("Settings initialized in settings.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> getSettings() {
        // If settings.json doesn't exist, create it.
        if (!new File("./settings.json").exists()) {
            initDefaultSettings();
        }

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
    private void updateCaptureDevice(ActionEvent event) {
        captureDeviceDropdown.setText(((MenuItem) event.getSource()).getText());
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
        settings.put("captureDevice", captureDeviceDropdown.getText());

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

    public static String getNativeLanguage() {
        Map<String, String> settings = getSettings();
        assert settings != null;
        return settings.get("native");
    }

    public static String getForeignLanguage() {
        Map<String, String> settings = getSettings();
        assert settings != null;
        return settings.get("foreign");
    }

    public static String getNativeCode() {
        Map<String, String> settings = getSettings();
        assert settings != null;
        return Languages.getStdCode(Languages.valueOf(settings.get("native").toUpperCase()));
    }

    public static String getForeignCode() {
        Map<String, String> settings = getSettings();
        assert settings != null;
        return Languages.getStdCode(Languages.valueOf(settings.get("foreign").toUpperCase()));
    }

    public static String getCaptureDevice() {
        Map<String, String> settings = getSettings();
        assert settings != null;
        return settings.get("captureDevice");
    }
}
