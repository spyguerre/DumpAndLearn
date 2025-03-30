package dal.graphic;

import javafx.scene.control.Alert;

public abstract class NotificationDisplayer {
    // Displays a simple popup to inform the user.
    public static void displayError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Error");
        alert.setHeaderText(errorMessage);

        // Block interaction with the rest of the app until popup is closed
        alert.showAndWait();
    }

    // Displays a simple popup to inform the user.
    public static void displayInfo(String infoMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(infoMessage);

        // Block interaction with the rest of the app until popup is closed
        alert.showAndWait();
    }
}
