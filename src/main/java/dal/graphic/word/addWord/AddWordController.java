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
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class AddWordController extends TranslationController {
    private final ObservableList<WordCell> wordsList = FXCollections.observableArrayList();

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<WordCell> searchListView;

    @FXML
    private VBox listViewVBox;

    @FXML
    private ScrollPane listViewScrollPane;

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
                WordCell selectedItem = searchListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // Delete the word it represents from the database and update the ListView
                    int cur = searchListView.getSelectionModel().getSelectedIndex(); // Current selected index
                    Word selectedWord = selectedItem.getWord();
                    Db.removeWord(selectedWord.getNative_(), selectedWord.getForeign());
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

        // Set the title of the columns for the ListView
        initListViewTitles();
    }

    private void initListViewTitles() {
        GridPane titles = new GridPane();
        // Divide the GridPane into 7 equal columns
        for (int i = 0; i < 7; i++) {
            titles.getColumnConstraints().add(new ColumnConstraints());
            // Set the percentage width of each column
            titles.getColumnConstraints().get(i).setPercentWidth(100.0 / 7);
        }
        // Add margin
        titles.setPadding(new Insets(0, 30, 0, 30));
        // Add the titles to the GridPane in Labels
        titles.add(new Label("Native"), 0, 0);
        titles.add(new Label("Foreign"), 1, 0);
        titles.add(new Label("Description"), 2, 0);
        titles.add(new Label("Added on"), 3, 0);
        titles.add(new Label("Times reviewed"), 4, 0);
        titles.add(new Label("Failed reviews"), 5, 0);
        titles.add(new Label("Last reviewed on"), 6, 0);
        // Center each Label in its cell
        for (int i = 0; i < 7; i++) {
            Label label = (Label) titles.getChildren().get(i);
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            GridPane.setHalignment(label, HPos.CENTER);
            GridPane.setValignment(label, VPos.CENTER);
        }
        // Add the titles to the top of the ListView
        listViewVBox.getChildren().addFirst(titles);

        // Bind the scrollpane's width to half the window's width
        listViewScrollPane.maxWidthProperty().bind(((Pane) root).widthProperty().divide(2).subtract(42));
    }

    private void updateSearchListView() {
        System.out.println("Updating search list...");

        wordsList.clear();
        wordsList.addAll(Db.searchForWords(searchTextField.getText(), WordType.ANY, 20).stream().map(WordCell::new).toList());
        // Bind each cell's width to the width of the ListView
        wordsList.forEach(wordCell -> wordCell.maxWidthProperty().bind(searchListView.widthProperty().subtract(20)));
    }
}
