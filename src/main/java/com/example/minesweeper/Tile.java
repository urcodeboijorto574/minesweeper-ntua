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

import java.io.IOException;
import java.util.List;

public class Tile extends StackPane {
    private final int x, y;
    private final boolean hasMine;
    private final boolean hasSupermine;
    public static boolean supermineExists;
    private boolean isOpen = false;
    private boolean isFlagged = false;
    private boolean isFlaggedPermanent = false;

    private final Rectangle border;
    private final Text text = new Text();
    private final Circle circle = new Circle();

    public Tile(int x, int y, int tileSize, boolean mine, boolean supermine) throws ContradictionException {
        this.x = x;
        this.y = y;
        this.hasMine = mine;
        this.hasSupermine = supermine;

        if (!this.hasMine && this.hasSupermine)
            throw new ContradictionException(
                    "CONTRADICTION: A tile that has a supermine technically also has a mine."
            );

        border = new Rectangle(tileSize - 1, tileSize - 1);
        border.setStroke(Color.LIGHTGRAY);

        text.setFont(Font.font(!hasMine ? 20 : 15));
        text.setStrokeWidth(5);
        text.setText(hasMine ? "M" : "");
        text.setVisible(false);
        text.setTextAlignment(TextAlignment.CENTER);

        circle.setCenterX(text.getX());
        circle.setCenterY(text.getY());
        circle.setVisible(false);
        circle.setFill(Color.BLACK);
        circle.setRadius(tileSize * ((3.0 / 4.0) / 2.0f));

        getChildren().addAll(border, circle, text);
        relocate((x * tileSize), (y * tileSize));

        setOnMouseClicked(event -> {
            switch (event.getButton()) {
                case PRIMARY -> open();
                case SECONDARY -> flag();
                case MIDDLE -> {
                    if (!isOpen)
                        return;

                    List<Tile> neighbors = MinesweeperApp.getNeighbors(this);

                    int neighborsFlagged = (int) neighbors.stream().filter(n -> n.isFlagged).count();
                    int neighborsMined = (int) neighbors.stream().filter(n -> n.hasMine).count();

                    if (neighborsFlagged == neighborsMined) {
                        open();
                        for (Tile neighbor : neighbors)
                            if (!neighbor.isFlagged)
                                neighbor.open();
                    }
                }
            }
        });
    }

    public void open() {
        if (!MinesweeperApp.gameStarted) {
            MinesweeperApp.timeCounter.timerStart();
            MinesweeperApp.gameStarted = true;
        } else if (isOpen || isFlaggedPermanent || MinesweeperApp.gameFinished)
            return;

        isOpen = true;
        MinesweeperApp.triesIncrease();
        text.setVisible(true);
        border.setFill(hasMine ? Color.DARKRED : Color.DARKGRAY);

        if (hasMine) {

            circle.setVisible(true);
            MinesweeperApp.timeCounter.timerCancel();
            MinesweeperApp.gameFinished = true;
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Game Over!");
            alert.setHeaderText("You landed on a mine!");
            alert.setContentText("Try again?");
            if (alert.showAndWait().orElseThrow() == ButtonType.OK) {
                MinesweeperApp.gameStarted = MinesweeperApp.gameFinished = false;
                try {
                    MinesweeperApp.initializeVariables(MinesweeperApp.fileName);
                } catch (IOException e) {
                    System.err.println("ERROR");
                    e.printStackTrace();
                    System.exit(5);
                }
                MinesweeperApp.restartScene();
            }

        } else if (MinesweeperApp.mineCounter.increaseOpenedTiles() == MinesweeperApp.mineCounter.getTilesSafeNumber()) {
            // TODO: player-wins-and-game-ends state
            MinesweeperApp.timeCounter.timerCancel();
            System.out.println("Congratulations! You won!");
            MinesweeperApp.gameFinished = true;
        } else if (text.getText().isEmpty()) {
            MinesweeperApp.getNeighbors(this).forEach(Tile::open);
        }
    }
    public void flag() {
        if (!MinesweeperApp.gameStarted) {
            MinesweeperApp.timeCounter.timerStart();
            MinesweeperApp.gameStarted = true;
        }

        if (isOpen || (MinesweeperApp.mineCounter.getRemainingNumber() == 0 && !isFlagged) || isFlaggedPermanent || MinesweeperApp.gameFinished)
            return;

        isFlagged = !isFlagged;

        if (!isFlagged) {
            border.setFill(Color.BLACK);
            MinesweeperApp.mineCounter.setText(MinesweeperApp.mineCounter.increaseRemainingNumber());
        } else {
            if (hasSupermine && MinesweeperApp.getTries() <= 4) {
                this.flagPermanent();
                List<Tile> sameRowColNeighbors = MinesweeperApp.getRowColNeighbors(this);
                for (Tile tile : sameRowColNeighbors)
                    if (tile.hasMine) tile.flagPermanent();
                    else              tile.open();
            } else {
                border.setFill(Color.ORANGERED);
                MinesweeperApp.mineCounter.setText(MinesweeperApp.mineCounter.decreaseRemainingNumber());
            }
        }
    }
    private void flagPermanent() {
        isFlagged = isFlaggedPermanent = true;
        border.setFill(Color.ORANGE);
        MinesweeperApp.mineCounter.setText(MinesweeperApp.mineCounter.decreaseRemainingNumber());
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public boolean getHasMine() { return hasMine; }
//    public boolean getHasSupermine() { return hasSupermine; }

    public void setTextColor(Color color) {
        text.setFill(color);
    }
    public void setText(int number) {
        text.setText(String.valueOf(number));
    }
}
