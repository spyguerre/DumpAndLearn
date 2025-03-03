package dal.graphic.song.playing;

import dal.graphic.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class TranslationPopupController extends Controller {

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

    @FXML
    void addNewWord(ActionEvent event) {

    }

}
