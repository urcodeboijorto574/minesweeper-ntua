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

    /**
     * Class constructor.
     *
     * @param x the x-coordinate of the tile inside its parent, measured in pixels
     * @param y the y-coordinate of the tile inside its parent, measured in pixels
     * @param tileSize the width and height of the tile in pixels
     * @param mine argument that states whether the tile will contain a mine
     * @param supermine argument that states whether the tile will contain a supermine
     * @throws ContradictionException if it is stated that the tile does not contain a mine but contains a supermine a ContradictionException is thrown
     */
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
                case PRIMARY -> { open(); MinesweeperApp.triesIncrease(); }
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
                            if (!neighbor.isFlagged) {
                                neighbor.open();
                                MinesweeperApp.triesIncrease();
                            }
                }
            }
        });
    }

    /**
     * Method that opens the tile. The tries made in the current game are NOT automatically increased.
     * If the tile contains a mine or is the last one that does not contain a mine,
     * the game ends, the score is being saved and an alert is shown asking the user to play Minesweeper again.
     * If the tile is not next to any mine-containing tile, it automatically opens the tiles next to it.
     * If the opening of the tile concludes in the game to be finished, the timer is stopped automatically.
     */
    public void open() {
        if (!MinesweeperApp.gameStarted) {
            MinesweeperApp.timeCounter.timerStart();
            MinesweeperApp.gameStarted = true;
        } else if (MinesweeperApp.gameFinished) {
            MinesweeperApp.endBecauseTimeRanOut();
            return;
        } else if (isOpen)
            return;

        MinesweeperApp.mineCounter.increaseOpenedTiles();
        isOpen = true;
        text.setVisible(true);
        border.setFill(hasMine ? Color.DARKRED : Color.DARKGRAY);

        if (hasMine) {
            MinesweeperApp.saveScore(false);

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
            MinesweeperApp.saveScore(true);

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

    /**
     * Method that flags the tile. The remaining number of unflagged mine-containing tiles in the game is automatically updated.
     * If the tile contains a supermine and no more than 4 tries have been made in the current game, all the other tiles
     * in the same row and column will open, if they do not contain a mine, or be flagged, if they do contain a mine.
     * The mines that are flagged permanently won't be able to be opened or flagged again in the current game.
     *
     */
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
                for (Tile tile : sameRowColNeighbors) {
                    if (tile.hasMine) tile.flagPermanent();
                    else tile.openNonRecursive();
                }
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

    /**
     * Method that returns the x-coordinate of the tile in relation to its parent.
     * @return x-coordinate of the tile in its parent
     */
    public int getX() { return x; }

    /**
     * Method that returns the y-coordinate of the tile in relation to its parent.
     * @return y-coordinate of the tile in its parent
     */
    public int getY() { return y; }

    /**
     * Method that returns true if the tile contains a mine or false if the tile does not contain a mine.
     * @return boolean value expressing the possession of a mine
     */
    public boolean getHasMine() { return hasMine; }

    /**
     * Method that sets the text's color to the specified color given as an argument.
     * @param color color that the text will be made to
     */
    public void setTextColor(Color color) { text.setFill(color); }

    /**
     * Method that sets the text to the specified number given as an argument.
     * @param number number that the text will have
     */
    public void setText(int number) { text.setText(String.valueOf(number)); }
}
