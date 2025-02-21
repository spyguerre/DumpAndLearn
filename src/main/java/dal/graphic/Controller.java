package dal.graphic;

import data.db.Db;
import javafx.fxml.FXML;
import javafx.scene.Node;

public abstract class Controller {
    @FXML
    protected Node root;

    @FXML
    public void quit() {
        System.out.println("Quitting...");
        Db.closeConnection();
        System.exit(0);
    }
}
