package com.example.minesweeper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class Tile extends StackPane {
    private final int x, y;
    private final boolean hasMine, hasSupermine;
    public static boolean supermineExists;
    private boolean isOpen = false, isFlagged = false;

    private final Rectangle border;
    private final Text text = new Text();
    private final Circle circle = new Circle();

    public Tile(int x, int y, int tileSize, boolean mine, boolean supermine) throws ContradictionException {
        this.x = x;
        this.y = y;
        this.hasMine = mine;
        this.hasSupermine = supermine;

        if (!this.hasMine && this.hasSupermine) {
            throw new ContradictionException("CONTRADICTION: A tile that has a supermine technically also has a mine.");
        }

        // Initialization of border, text, circle
        {
            border = new Rectangle(tileSize - 1, tileSize - 1);
            border.setStroke(Color.LIGHTGRAY);
            border.setFill(Color.rgb(43, 43, 43));

            text.setFont(Font.font(!hasMine ? tileSize * 0.571 : tileSize / 2.0));
            text.setStrokeWidth(5);
            text.setText(hasMine ? "M" : "");
            text.setVisible(false);
            text.setTextAlignment(TextAlignment.CENTER);

            circle.setCenterX(text.getX());
            circle.setCenterY(text.getY());
            circle.setVisible(false);
            circle.setFill(Color.BLACK);
            circle.setRadius(tileSize * ((3.0 / 4.0) / 2.0f));
        }

        getChildren().addAll(border, circle, text);
        relocate((x * tileSize), (y * tileSize));

        setOnMouseClicked(event -> {
            switch (event.getButton()) {
                case PRIMARY -> open();
                case SECONDARY -> flag();
                case MIDDLE -> {
                    if (MinesweeperApp.gameFinished) {
                        MinesweeperApp.endBecauseTimeRanOut();
                        return;
                    } else if (!isOpen)
                        return;

                    List<Tile> neighbors = MinesweeperApp.getNeighbors(this);

                    int neighborsFlagged = (int) neighbors.stream().filter(n -> n.isFlagged).count();
                    int neighborsMined = (int) neighbors.stream().filter(n -> n.hasMine).count();

                    if (neighborsFlagged == neighborsMined)
                        for (Tile neighbor : neighbors)
                            if (!neighbor.isFlagged)
                                neighbor.open();
                }
            }
        });
    }

    public void open() {
        if (!MinesweeperApp.gameStarted) {
            MinesweeperApp.timeCounter.timerStart();
            MinesweeperApp.gameStarted = true;
        } else if (MinesweeperApp.gameFinished) {
            MinesweeperApp.endBecauseTimeRanOut();
            return;
        } else if (isOpen) {
            return;
        }

        MinesweeperApp.mineCounter.increaseOpenedTiles();
        System.out.println("Currently opened tiles; " + MinesweeperApp.mineCounter.getTilesOpenNumber());
        isOpen = true;
        MinesweeperApp.triesIncrease();
        text.setVisible(true);
        border.setFill(hasMine ? Color.DARKRED : Color.DARKGRAY);

        if (hasMine) {
            MinesweeperApp.saveScore();

            MinesweeperApp.timeCounter.timerCancel();
            circle.setVisible(true);
            MinesweeperApp.gameFinished = true;
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Game Over!");
            alert.setHeaderText("You landed on a mine!");
            alert.setContentText("Try again?");
            if (alert.showAndWait().orElseThrow() == ButtonType.OK)
                MinesweeperApp.restartGame();

        } else if (MinesweeperApp.mineCounter.getTilesOpenNumber() == MinesweeperApp.mineCounter.getTilesSafeNumber()) {
            MinesweeperApp.timeCounter.timerCancel();
            System.out.println("Congratulations! You won!");
            MinesweeperApp.gameFinished = true;

            MinesweeperApp.saveScore();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("YOU WON!");
            alert.setHeaderText("Congratulations! You won the game!");
            alert.setContentText("Would you like to play again?");

            if (alert.showAndWait().orElseThrow() == ButtonType.OK)
                MinesweeperApp.restartGame();
        } else if (text.getText().isEmpty())
            MinesweeperApp.getNeighbors(this).forEach(Tile::open);
    }
    private void openNonRecursive() {
        isOpen = true;
        text.setVisible(true);
        border.setFill(Color.DARKGRAY);
    }
    public void flag() {
        if (!MinesweeperApp.gameStarted) {
            MinesweeperApp.timeCounter.timerStart();
            MinesweeperApp.gameStarted = true;
        } else if (MinesweeperApp.gameFinished) {
            MinesweeperApp.endBecauseTimeRanOut();
            return;
        }

        if (isOpen || (MinesweeperApp.mineCounter.getRemainingNumber() == 0 && !isFlagged))
            return;

        isFlagged = !isFlagged;

        if (!isFlagged) {
            border.setFill(Color.rgb(43, 43, 43));
            MinesweeperApp.mineCounter.setText(MinesweeperApp.mineCounter.increaseRemainingNumber());
        } else {
            if (hasSupermine && MinesweeperApp.getTries() <= 4) {
                this.flagPermanent();
                List<Tile> sameRowColNeighbors = MinesweeperApp.getRowColNeighbors(this);
                for (Tile tile : sameRowColNeighbors)
                    if (tile.hasMine) tile.flagPermanent();
                    else              tile.openNonRecursive();
            } else {
                border.setFill(Color.ORANGERED);
                MinesweeperApp.mineCounter.setText(MinesweeperApp.mineCounter.decreaseRemainingNumber());
            }
        }
    }
    private void flagPermanent() {
        isOpen = isFlagged = true;
        border.setFill(Color.ORANGE);
        MinesweeperApp.mineCounter.setText(MinesweeperApp.mineCounter.decreaseRemainingNumber());
    }
    public void reveal() {
        text.setVisible(true);
        circle.setVisible(true);
        border.setFill(Color.DARKRED);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public boolean getHasMine() { return hasMine; }

    public void setTextColor(Color color) { text.setFill(color); }
    public void setText(int number) { text.setText(String.valueOf(number)); }
}
