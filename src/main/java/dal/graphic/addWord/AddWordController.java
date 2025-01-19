package dal.graphic.addWord;

import dal.Db;
import dal.word.Word;
import dal.word.WordType;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class AddWordController {
    ObservableList<String> wordsList = FXCollections.observableArrayList();

    @FXML
    private TextField foreignTextField;

    @FXML
    private TextField nativeTextBox;

    @FXML
    private TextField searchTextBox;

    @FXML
    private ListView<String> searchListView;

    @FXML
    public void initialize() {
        // Set listener to update the scrollPane
        PauseTransition pause = new PauseTransition(Duration.millis(500));
        searchTextBox.textProperty().addListener((observable, oldValue, newValue) -> {
            // Reset the debounce timer whenever the text changes
            pause.setOnFinished(event -> {
                updateSearchListView();
            });
            pause.playFromStart();
        });

        // Attach the words list to the ListView and fill it
        searchListView.setItems(wordsList);
        updateSearchListView();
    }

    @FXML
    void deleteLast(ActionEvent event) {

    }

    @FXML
    void addNewWord() {
        Db.insertNewWord(nativeTextBox.getText(), foreignTextField.getText());
        updateSearchListView();
    }

    @FXML
    void quit(ActionEvent event) {
        Db.closeConnection();
        System.exit(0);
    }

    private void updateSearchListView() {
        wordsList.clear();
        wordsList.addAll(
                Db.searchForWords(searchTextBox.getText(), WordType.ANY, 20).stream().map(
                        (Word word) -> word.getNative_() + " = " + word.getForeign()
                ).toList()
        );
    }
}
