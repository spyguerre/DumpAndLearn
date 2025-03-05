package dal.graphic.song.playing;

import dal.graphic.word.addWord.TranslationController;

public class TranslationPopupController extends TranslationController {
    // Sets str to the foreign TextField, and the hidden description of the popup.
    public void initTranslation(String str) {
        foreignTranslateTextField.setText(str.trim());
        descriptionTextField.setText(str.trim());
    }
}
