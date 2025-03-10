package dal.graphic.song.playing;

import dal.graphic.word.addWord.TranslationController;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class TranslationPopupController extends TranslationController {
    // Sets str to the foreign TextField, and the hidden description of the popup.
    public void initTranslation(String str) {
        foreignTranslateTextField.setText(str.trim());
        descriptionTextField.setText(str.trim());
        // Bind root's preferred width to half of the window's width
        ((VBox) root).prefWidthProperty().bind(root.getScene().getWindow().widthProperty().multiply(0.8));
        ((VBox) root).prefHeightProperty().bind(root.getScene().getWindow().heightProperty().multiply(0.8));
        // Bind VBox position to center it dynamically inside the Pane
        root.layoutXProperty().bind(((Pane) root.getParent()).widthProperty().subtract(((VBox) root).widthProperty()).divide(2));
        root.layoutYProperty().bind(((Pane) root.getParent()).heightProperty().subtract(((VBox) root).heightProperty()).divide(2));    }
}
