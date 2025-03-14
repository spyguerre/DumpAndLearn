package dal.graphic.ocr;

import dal.data.ocr.OCR;
import dal.data.translation.Translator;
import dal.graphic.Controller;
import dal.graphic.Languages;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.graphic.general.SettingsController;
import dal.graphic.song.playing.TranslationPopupController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.image.BufferedImage;

public class OCRController extends Controller {
    @FXML
    private TextArea foreignTextArea;

    @FXML
    private TextArea nativeTextArea;

    @FXML
    private StackPane popupContainerStackPane;

    @FXML
    private Rectangle dimBackground;

    @FXML
    private Pane popupContainerPane;

    @FXML
    public void initialize() {
        super.initialize();

        // Attach listener to trigger translation on text highlighting.
        PauseTransition pause = new PauseTransition(Duration.millis(742)); // Timer to wait for the user to finish highlighting.
        foreignTextArea.selectedTextProperty().addListener((obs, oldText, newText) -> {
            if (!newText.isEmpty()) {
                pause.setOnFinished(event -> {
                    System.out.println("User highlighted text: " + newText.substring(0, Math.min(newText.length(), 42)) + (newText.length() > 42 ? "..." : ""));
                    showTranslationPopup(newText, true);
                });
                pause.playFromStart(); // Restart the timer on every change
            }
        });

        nativeTextArea.selectedTextProperty().addListener((obs, oldText, newText) -> {
            if (!newText.isEmpty()) {
                pause.setOnFinished(event -> {
                    System.out.println("User highlighted text: " + newText.substring(0, Math.min(newText.length(), 42)) + (newText.length() > 42 ? "..." : ""));
                    showTranslationPopup(newText, false);
                });
                pause.playFromStart(); // Restart the timer on every change
            }
        });

        // Automatically resize the rectangle to dim the background
        dimBackground.widthProperty().bind(popupContainerStackPane.widthProperty());
        dimBackground.heightProperty().bind(popupContainerStackPane.heightProperty());

        // Hide the popup when rectangle is clicked.
        dimBackground.setOnMouseClicked(event -> popupContainerStackPane.setVisible(false));
    }

    public void initText(BufferedImage image) {
        System.out.println("Initializing text with selected image...");

        // Bind the popup Pane size
        popupContainerPane.prefWidthProperty().bind(this.root.getScene().getWindow().widthProperty().multiply(0.8));
        popupContainerPane.prefHeightProperty().bind(this.root.getScene().getWindow().heightProperty().multiply(0.8));

        new Thread(() -> {
            // Perform OCR.
            System.out.println("Recognizing text...");
            String text = OCR.read(image, Languages.valueOf(SettingsController.getForeignLanguage().toUpperCase()));

            // Set the foreign text.
            System.out.println("Setting the foreign text...");
            Platform.runLater(() ->
                    foreignTextArea.setText(text.replaceAll("\\r\\n|\\r|\\n", "\n"))
            );

            // Translate the text and set it in the nativeTextArea.
            String translation = Translator.translate(text);
            Platform.runLater(() ->
                    nativeTextArea.setText(translation)
            );
        }).start();
    }

    public void showTranslationPopup(String str, boolean isForeignText) {
        // Load the new scene in the translationPopupContainer Pane.
        popupContainerPane.getChildren().clear(); // Remove previous content
        TranslationPopupController popupController = (TranslationPopupController) SceneManager.loadSceneInContainer(SceneType.TRANSLATION_POPUP, popupContainerPane);
        popupContainerStackPane.setVisible(true);
        assert popupController != null;
        popupController.initTranslation(str, isForeignText);
    }

    @FXML
    private void scanSomeText() {
        SceneManager.switchScene(SceneType.START_OCR, (Stage) root.getScene().getWindow(), new int[]{(int)((Pane)root).getWidth(), (int)((Pane)root).getHeight()});
    }
}
