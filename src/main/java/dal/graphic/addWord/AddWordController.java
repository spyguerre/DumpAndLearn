package dal.graphic.addWord;

import dal.Db;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class AddWordController {
    @FXML
    private TextField foreignTextField;

    @FXML
    private TextField nativeTextBox;

    @FXML
    private TextField searchTextBox;

    @FXML
    private ScrollPane searchScrollPane;

    @FXML
    public void initialize() {
        // Set listener to update the scrollPane
        PauseTransition pause = new PauseTransition(Duration.millis(500));
        searchTextBox.textProperty().addListener((observable, oldValue, newValue) -> {
            // Reset the debounce timer whenever the text changes
            pause.setOnFinished(event -> {
                updateSearchScrollPane(newValue);
            });
            pause.playFromStart();
        });
    }

    @FXML
    void deleteLast(ActionEvent event) {

    }

    @FXML
    void quit(ActionEvent event) {
        Db.closeConnection();
        System.exit(0);
    }

    private void updateSearchScrollPane(String query) {
        System.out.println(query);
    }
}
