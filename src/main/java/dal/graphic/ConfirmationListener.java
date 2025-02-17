package dal.graphic;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Listener that listens for enter keyboard inputs and that takes as an argument a method to execute when created.
 */
public class ConfirmationListener implements EventHandler<KeyEvent> {
    private final Runnable action;

    public ConfirmationListener(Runnable action) {
        this.action = action;
    }

    @Override
    public void handle(KeyEvent event) {
        // Check if the ENTER key is pressed
        if (event.getCode() == KeyCode.ENTER) {
            action.run();
        }
    }
}
