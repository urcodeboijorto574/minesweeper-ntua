package com.example.minesweeper;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.File;

public class LoadController {
    @FXML
    private TextField textField;
    @FXML
    private void submitFilePath() {
        MinesweeperApp.fileName = textField.getText() + ".txt";
        if (!(new File(".\\medialab\\" + MinesweeperApp.fileName)).exists()) {
            System.out.println("You entered an invalid filename. " +
                    "Create this file immediately using the 'Create' item " +
                    "or click again the 'Load' item to enter the file name correctly.");
        }
        MinesweeperApp.loadStage.close();
    }
}
