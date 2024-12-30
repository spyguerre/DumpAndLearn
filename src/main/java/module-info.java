module com.example.dal {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;

    opens dal to javafx.fxml;
    exports dal;
}