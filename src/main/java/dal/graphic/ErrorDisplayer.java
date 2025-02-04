package dal.graphic;

import javafx.scene.control.Alert;

public abstract class ErrorDisplayer {
    // Displays a simple popup to inform the user.
    public static void displayError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Error");
        alert.setHeaderText(errorMessage);

        // Block interaction with the rest of the app until popup is closed
        alert.showAndWait();
    }
}
