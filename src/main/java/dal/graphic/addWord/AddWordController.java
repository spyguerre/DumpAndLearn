package dal.graphic.addWord;

import dal.Db;
import dal.graphic.Controller;
import dal.graphic.ErrorDisplayer;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.word.Word;
import dal.word.WordType;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AddWordController extends Controller {
    private final ObservableList<String> wordsList = FXCollections.observableArrayList();
    private final String wordsSeparator = " = ";

    @FXML
    private TextField foreignTextField;

    @FXML
    private TextField nativeTextField;

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

        nativeTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this));
        foreignTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this));

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

        Db.insertNewWord(nativeTextField.getText(), foreignTextField.getText());
        resetInputTextFields();
        updateSearchListView();
    }

    @FXML
    private void startReviewSession() {
        SceneManager.switchScene(SceneType.START_REVIEW, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
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
        nativeTextField.setText("");
        foreignTextField.setText("");
    }
}
