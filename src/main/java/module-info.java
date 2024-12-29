module com.example.dal {
    requires javafx.controls;
    requires javafx.fxml;


    opens dal to javafx.fxml;
    exports dal;
}