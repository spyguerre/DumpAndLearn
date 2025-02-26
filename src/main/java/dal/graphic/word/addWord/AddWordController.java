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
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

public class AddWordController extends Controller {
    private final ObservableList<String> wordsList = FXCollections.observableArrayList();
    private final String wordsSeparator = " = ";

    @FXML
    private TextField foreignTextField;

    @FXML
    private TextField nativeTextField;

    @FXML
    private TextField descriptionTextField;

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<String> searchListView;

    @FXML
    private void initialize() {
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

        nativeTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::addNewWord));
        foreignTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::addNewWord));
        descriptionTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::addNewWord));

        // Attach the words list to the ListView and fill it
        searchListView.setItems(wordsList);
        updateSearchListView();
    }

    @FXML
    private void deleteLast(ActionEvent event) {

    }

    @FXML
    public void addNewWord() {
        System.out.println("Adding new word...");

        // Ensure that there is a word in both textFields
        if (nativeTextField.getText().isEmpty() || foreignTextField.getText().isEmpty()) {
            System.out.println("Couldn't find native or foreign word.");
            ErrorDisplayer.displayError("Please fill both languages for the word.");
            return;
        }

        String description = null;
        if (!descriptionTextField.getText().isEmpty()) {
            description = descriptionTextField.getText();
        }

        Db.insertNewWord(nativeTextField.getText(), foreignTextField.getText(), description);
        resetInputTextFields();
        updateSearchListView();
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

    private void resetInputTextFields() {
        nativeTextField.clear();
        foreignTextField.clear();
        descriptionTextField.clear();

        nativeTextField.requestFocus();
    }
}
