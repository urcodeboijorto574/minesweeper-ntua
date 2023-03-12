package com.example.minesweeper;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Controller {

    private Stage stage;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Button startButton;

    @FXML
    private Rectangle minesCount;
    @FXML
    private Rectangle timer;


    @FXML
    public void mine(ActionEvent e) {
        System.out.println("MINED");
    }

    @FXML
    public void flag(ActionEvent e) {
        System.out.println("FLAGGED");
    }
}
