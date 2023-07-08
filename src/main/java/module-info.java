module com.example.minesweeper2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.minesweeper to javafx.fxml;
    exports com.example.minesweeper;
}