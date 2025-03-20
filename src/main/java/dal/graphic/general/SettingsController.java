package dal.graphic.general;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sarxos.webcam.Webcam;
import dal.graphic.Controller;
import dal.graphic.ErrorDisplayer;
import dal.data.Languages;
import dal.graphic.word.startReview.ReviewPreference;
import dal.graphic.word.startReview.WriteIn;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsController extends Controller {
    @FXML
    private MenuButton foreignDropDown;

    @FXML
    private MenuButton nativeDropDown;

    @FXML
    private MenuButton captureDeviceDropdown;

    @FXML
    private MenuButton defaultWordCountDropdown;

    @FXML
    private MenuButton defaultPreferenceDropdown;

    @FXML
    private MenuButton defaultWriteInDropdown;

    @FXML
    private VBox mainVBox;

    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    protected void initialize() {
        super.initialize();

        // Initialize the default settings if necessary.
        File settingsFile = new File("settings.json");
        if (!settingsFile.exists()) {
            initDefaultSettings();
        }

        // Update the dropdowns' text to match known settings.
        loadSettings();

        // Update capture device options
        for (Webcam webcam: Webcam.getWebcams()) {
            // Create and add menuItem
            MenuItem item = new MenuItem(webcam.getName());
            captureDeviceDropdown.getItems().add(item);
        }

        // Add action to all menuItems
        List<MenuButton> allMenuButtons = new ArrayList<>();
        findMenuButtons(mainVBox, allMenuButtons); // Fills the list
        for (MenuButton menuButton: allMenuButtons) {
            for (MenuItem menuItem: menuButton.getItems()) {
                menuItem.setOnAction(event -> this.updateDropdown(event, menuButton));
            }
        }

        // Bind mainVBox to its Parent.
        mainVBox.prefWidthProperty().bind(mainScrollPane.widthProperty());
        mainVBox.prefHeightProperty().bind(mainScrollPane.heightProperty());
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

    private static void initDefaultSettings() {
        // Create a dictionary with the default settings.
        Map<String, Object> settings = new HashMap<>();

        settings.put("captureDevice", "None");

        Map<String, String> languageSettings = new HashMap<>();
        languageSettings.put("native", "French");
        languageSettings.put("foreign", "English");
        settings.put("languageSettings", languageSettings);

        Map<String, String> preferredReviewSettings = new HashMap<>();
        preferredReviewSettings.put("wordCount", "10");
        preferredReviewSettings.put("reviewPreference", "Recent");
        preferredReviewSettings.put("writeIn", "Both");
        settings.put("preferredReviewSettings", preferredReviewSettings);

        // Convert to JSON and save to a file.
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("settings.json"), settings);
            System.out.println("Settings initialized in settings.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        captureDeviceDropdown.setText(getCaptureDevice());

        nativeDropDown.setText(getNativeLanguage());
        foreignDropDown.setText(getForeignLanguage());

        defaultWordCountDropdown.setText(String.valueOf(getDefaultWordCount()));
        defaultPreferenceDropdown.setText(ReviewPreference.getString(getDefaultPreference()));
        defaultWriteInDropdown.setText(WriteIn.getString(getDefaultWriteIn()));
    }

    private static Map<String, Object> getSettings() {
        // If settings.json doesn't exist, create it.
        if (!new File("./settings.json").exists()) {
            initDefaultSettings();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Read JSON file and convert it to a Map<String, String>
            Map<String, Object> settings = objectMapper.readValue(
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
    private void updateDropdown(ActionEvent event, MenuButton dropdown) {
        dropdown.setText(((MenuItem) event.getSource()).getText());
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
        Map<String, Object> settings = new HashMap<>();
        settings.put("captureDevice", captureDeviceDropdown.getText());

        Map<String, String> languageSettings = new HashMap<>();
        languageSettings.put("native", nativeDropDown.getText());
        languageSettings.put("foreign", foreignDropDown.getText());
        settings.put("languageSettings", languageSettings);

        Map<String, String> preferredReviewSettings = new HashMap<>();
        preferredReviewSettings.put("wordCount", defaultWordCountDropdown.getText());
        preferredReviewSettings.put("reviewPreference", defaultPreferenceDropdown.getText());
        preferredReviewSettings.put("writeIn", defaultWriteInDropdown.getText());
        settings.put("preferredReviewSettings", preferredReviewSettings);

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
        Map<String, Object> settings = getSettings();
        assert settings != null;
        return (String) ((Map<?, ?>) settings.get("languageSettings")).get("native");
    }

    public static String getForeignLanguage() {
        Map<String, Object> settings = getSettings();
        assert settings != null;
        return (String) ((Map<?, ?>) settings.get("languageSettings")).get("foreign");
    }

    public static String getNativeCode() {
        return Languages.getStdCode(Languages.valueOf(getNativeLanguage().toUpperCase()));
    }

    public static String getForeignCode() {
        return Languages.getStdCode(Languages.valueOf(getForeignLanguage().toUpperCase()));
    }

    public static String getCaptureDevice() {
        Map<String, Object> settings = getSettings();
        assert settings != null;
        return (String) settings.get("captureDevice");
    }

    public static int getDefaultWordCount() {
        Map<String, Object> settings = getSettings();
        assert settings != null;
        return Integer.parseInt((String) ((Map<?, ?>) settings.get("preferredReviewSettings")).get("wordCount"));
    }

    public static ReviewPreference getDefaultPreference() {
        Map<String, Object> settings = getSettings();
        assert settings != null;
        return ReviewPreference.getReviewPreference((String) ((Map<?, ?>) settings.get("preferredReviewSettings")).get("reviewPreference"));
    }

    public static WriteIn getDefaultWriteIn() {
        Map<String, Object> settings = getSettings();
        assert settings != null;
        return WriteIn.getWriteIn((String) ((Map<?, ?>) settings.get("preferredReviewSettings")).get("writeIn"));
    }
}
