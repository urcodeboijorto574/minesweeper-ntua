package com.example.minesweeper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class RoundsController implements Initializable {
    @FXML private ListView<String>
            scoreListView1,
            scoreListView2,
            scoreListView3,
            scoreListView4,
            scoreListView5;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        for (int i = 1; i <= 5; ++i) {
            String mineNumberString, triesNumberString, secondsPlayedNumberString, winnerString;
            String emptyFieldString = "(empty field)";
            boolean scoreExists = i <= MinesweeperApp.scoreTable.size();
            int indexList = i - 1;

            // 1) Mine Number
            mineNumberString = "Total number of mines: " +
                    (scoreExists ?
                            MinesweeperApp.scoreTable.get(indexList).minesTotalNumber() : emptyFieldString);
            // 2) Tries Number
            triesNumberString = "Total number of tries: " +
                    (scoreExists ?
                            MinesweeperApp.scoreTable.get(indexList).tries() : emptyFieldString);
            // 3) Time Played Number
            secondsPlayedNumberString = "Total number of seconds played: " +
                    (scoreExists ?
                            MinesweeperApp.scoreTable.get(indexList).timePlayedSeconds() : emptyFieldString);
            // 4) Winner
            winnerString = "Winner: " +
                    (scoreExists ?
                            (MinesweeperApp.scoreTable.get(indexList).playerWon() ? "You" : "Computer") : emptyFieldString);
            // Accumulate information
            String[] accumulator = { mineNumberString, triesNumberString, secondsPlayedNumberString, winnerString };

            switch (i) {
                case 1 -> scoreListView1.getItems().addAll(accumulator);
                case 2 -> scoreListView2.getItems().addAll(accumulator);
                case 3 -> scoreListView3.getItems().addAll(accumulator);
                case 4 -> scoreListView4.getItems().addAll(accumulator);
                case 5 -> scoreListView5.getItems().addAll(accumulator);
            }
        }
    }

    @FXML
    private void goBack() {
        MinesweeperApp.roundsStage.close();
    }
}
