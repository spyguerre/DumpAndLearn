package dal.graphic.addWord;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ConfirmationListener implements EventHandler<KeyEvent> {
    private final AddWordController addWordController;

    public ConfirmationListener(AddWordController addWordController) {
        this.addWordController = addWordController;
    }

    @Override
    public void handle(KeyEvent event) {
        // Check if the ENTER key is pressed
        if (event.getCode() == KeyCode.ENTER) {
            addWordController.addNewWord();
        }
    }
}
