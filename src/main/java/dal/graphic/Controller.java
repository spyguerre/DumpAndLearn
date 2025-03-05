package dal.graphic;

import dal.data.db.Db;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public abstract class Controller {
    @FXML
    protected Node root;

    @FXML
    protected void mainMenu() {
        SceneManager.switchScene(SceneType.MAIN_MENU, (Stage) root.getScene().getWindow(), new int[]{(int)((Pane)root).getWidth(), (int)((Pane)root).getHeight()});
    }

    @FXML
    private void settings() {
        SceneManager.switchScene(SceneType.SETTINGS, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
    }

    @FXML
    protected void quit() {
        System.out.println("Quitting...");
        Db.closeConnection();
        System.exit(0);
    }
}
