package dal.graphic;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Listener that listens for specific key combinations and executes a method when the combination is detected.
 */
public class KeyboardListener implements EventHandler<KeyEvent> {
    private final Runnable action;
    private final KeyCode keyCode;
    private final boolean shiftRequired;
    private final boolean ctrlRequired;
    private final boolean altRequired;

    public KeyboardListener(Runnable action) {
        this(action, KeyCode.ENTER, false, false, false);
    }

    public KeyboardListener(Runnable action, KeyCode keyCode) {
        this(action, keyCode, false, false, false);
    }

    public KeyboardListener(Runnable action, KeyCode keyCode, boolean shiftRequired, boolean ctrlRequired, boolean altRequired) {
        this.action = action;
        this.keyCode = keyCode;
        this.shiftRequired = shiftRequired;
        this.ctrlRequired = ctrlRequired;
        this.altRequired = altRequired;
    }

    @Override
    public void handle(KeyEvent event) {
        // Check if the pressed key matches the specified key and modifiers
        if (event.getCode() == keyCode &&
                event.isShiftDown() == shiftRequired &&
                event.isControlDown() == ctrlRequired &&
                event.isAltDown() == altRequired) {
            action.run();
        }
    }
}
