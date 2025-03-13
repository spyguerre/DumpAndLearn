package dal.graphic;

import dal.data.db.Db;
import dal.graphic.general.SettingsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public abstract class Controller {
    @FXML
    protected Node root;

    @FXML
    protected void initialize() {
        customizeNativeAndForeignTexts();
    }

    @FXML
    protected void mainMenu() {
        SceneManager.switchScene(SceneType.MAIN_MENU, (Stage) root.getScene().getWindow(), new int[]{(int)((Pane)root).getWidth(), (int)((Pane)root).getHeight()});
    }

    @FXML
    protected void settings() {
        SceneManager.switchScene(SceneType.SETTINGS, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
    }

    @FXML
    protected void quit() {
        System.out.println("Quitting...");
        Db.closeConnection();
        Platform.exit();
        System.exit(0);
    }

    protected void customizeNativeAndForeignTexts() {
        String nativeLanguage = SettingsController.getNativeLanguage();
        String foreignLanguage = SettingsController.getForeignLanguage();
        Platform.runLater(() -> {
            for (Node node : root.lookupAll(".nativeText")) {
                if (node instanceof Text textNode) {
                    textNode.setText(nativeLanguage);
                }
            }
            for (Node node : root.lookupAll(".foreignText")) {
                if (node instanceof Text textNode) {
                    textNode.setText(foreignLanguage);
                }
            }
        });
    }
}
