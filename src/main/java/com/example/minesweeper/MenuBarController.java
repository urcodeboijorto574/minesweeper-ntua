package com.example.minesweeper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MenuBarController {

    private Stage stage;
    @FXML
    private BorderPane borderPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu applicationMenu, detailsMenu;
    @FXML
    private MenuItem createItem, loadItem, startItem, exitItem, roundsItem, solutionItem;

    public void exitFromMenuItem(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("You're about to exit!");
        alert.setContentText("Are you sure you want to exit Minesweeper?");

        if (alert.showAndWait().orElseThrow() == ButtonType.OK) {
            stage = (Stage) borderPane.getScene().getWindow();
            System.out.println("You successfully exited the application!");
            stage.close();
        }
    }
}
