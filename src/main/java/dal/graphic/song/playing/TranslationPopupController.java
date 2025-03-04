package dal.graphic.song.playing;

import dal.graphic.word.addWord.TranslationController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class TranslationPopupController extends TranslationController {

    @FXML
    private TextField foreignTextField;

    @FXML
    private TextField foreignTranslateTextField;

    @FXML
    private TextField nativeTextField;

    @FXML
    private TextField nativeTranslateTextField;

    @FXML
    private VBox root;

    // Sets str to the foreign TextField of the popup.
    public void initTranslation(String str) {
         foreignTranslateTextField.setText(str);
    }

    @FXML
    private void addNewWord() {

    }
}
