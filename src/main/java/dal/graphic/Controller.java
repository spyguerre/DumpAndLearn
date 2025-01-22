package dal.graphic;

import dal.Db;
import javafx.fxml.FXML;
import javafx.scene.Node;

public abstract class Controller {
    @FXML
    protected Node root;

    @FXML
    public void quit() {
        Db.closeConnection();
        System.exit(0);
    }
}
