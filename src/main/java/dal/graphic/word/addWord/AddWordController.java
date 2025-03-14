package dal.graphic.word.addWord;

import dal.data.db.Db;
import dal.graphic.*;
import dal.data.word.Word;
import dal.data.word.WordType;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

public class AddWordController extends TranslationController {
    private final ObservableList<String> wordsList = FXCollections.observableArrayList();
    private final String wordsSeparator = " = ";

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<String> searchListView;

    @FXML
    protected void initialize() {
        super.initialize();

        // Set listener to update the scrollPane
        PauseTransition pause = new PauseTransition(Duration.millis(500));
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Reset the debounce timer whenever the text changes
            pause.setOnFinished(event -> updateSearchListView());
            pause.playFromStart();
        });

        // Listen for delete events in the ListView
        searchListView.setOnKeyPressed(event -> {
            // Check if the Delete key is pressed
            if (event.getCode() == KeyCode.DELETE) {
                // Get the currently selected item
                String selectedItem = searchListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // Delete the word it represents from the database and update the ListView
                    int cur = searchListView.getSelectionModel().getSelectedIndex(); // Current selected index
                    String[] words = selectedItem.split(wordsSeparator);
                    Db.removeWord(words[0], words[1]);
                    updateSearchListView();

                    // Select next element if it exists, else the previous, if it exists; after updating the listView.
                    int size = searchListView.getItems().size();
                    if (cur < size) {
                        searchListView.getSelectionModel().select(cur);
                    } else if (cur > 0) {
                        searchListView.getSelectionModel().selectFirst();
                    }
                }
            }
        });

        // Attach the words list to the ListView and fill it
        searchListView.setItems(wordsList);
        updateSearchListView();
    }

    @FXML
    private void deleteLast(ActionEvent event) {

    }

    private void updateSearchListView() {
        System.out.println("Updating search list...");

        wordsList.clear();
        wordsList.addAll(
                Db.searchForWords(searchTextField.getText(), WordType.ANY, 20).stream().map(
                        (Word word) -> word.getNative_() + wordsSeparator + word.getForeign()
                ).toList()
        );
    }
}
