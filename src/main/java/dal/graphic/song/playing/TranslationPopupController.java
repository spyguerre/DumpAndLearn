package dal.graphic.song.playing;

import dal.graphic.word.addWord.TranslationController;
import javafx.fxml.FXML;

public class TranslationPopupController extends TranslationController {
    @FXML
    protected void initialize() {
        super.initialize();


    }

    // Sets str to the foreign TextField, and the hidden description of the popup.
    public void initTranslation(String str) {
        foreignTranslateTextField.setText(str);
        descriptionTextField.setText(str);
    }
}
