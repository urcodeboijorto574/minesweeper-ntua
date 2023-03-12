package com.example.minesweeper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

import java.io.File;
import java.io.FileNotFoundException;

public class MinesweeperApp extends Application {

    private static final int TILE_SIZE = 35;
    private static int WINDOW_WIDTH, WINDOW_HEIGHT, X_TILES, Y_TILES;
    private static Tile[][] grid;

    private static int totalNumberOfMines, tries = 0;
    private static boolean superMineExists;

    private static int numberOfSeconds;
    private static Timer timer = new Timer();
    TimerTask task = new TimerTask() {

        int counter = numberOfSeconds;
        @Override
        public void run() {
            if (counter > 0) {
                System.out.println(counter + " seconds");
                --counter;
            } else {
                System.out.println("HAPPY NEW YEAR!");
                timer.cancel();
            }
        }
    };

    private static void triesIncrease() {
        ++tries;
    }

    private class Tile extends StackPane {
        private final int x, y;
        private final boolean hasMine;
        private final boolean hasSuperMine;
        private boolean isOpen = false;
        private boolean isFlagged = false;
        private boolean isFlaggedPermanent = false;

        private final Rectangle border = new Rectangle(TILE_SIZE - 1, TILE_SIZE - 1);
        private final Text text = new Text();

        public Tile(int x, int y, boolean mine, boolean superMine) {
            this.x = x;
            this.y = y;
            this.hasMine = mine;
            this.hasSuperMine = superMine;

            border.setStroke(Color.LIGHTGRAY);

            text.setFont(Font.font(20));
            text.setText(hasMine ? "X" : "");
            text.setVisible(false);
            text.setTextAlignment(TextAlignment.CENTER);

            getChildren().addAll(border, text);

            relocate((x * TILE_SIZE), (y * TILE_SIZE));

            setOnMouseClicked(event -> {
                switch (event.getButton()) {
                    case PRIMARY -> open();
                    case SECONDARY -> flag();
                    case MIDDLE -> {
                        if (!isOpen) return;

                        List<Tile> neighbors = getNeighbors(this);

                        int neighborsFlagged = (int) neighbors.stream().filter(n -> n.isFlagged).count();
                        int neighborsMined = (int) neighbors.stream().filter(n -> n.hasMine).count();

                        System.out.println("neighborsFlagged: " + neighborsFlagged);
                        System.out.println("NeighborsMined: " + neighborsMined);

                        if (neighborsFlagged == neighborsMined) {
                            open();
                            for (Tile neighbor : neighbors)
                                if (!neighbor.hasMine) neighbor.open();
                        }
                    }
                }
            });
        }

        public void open() {
            if (isOpen)
                return;

            if (hasMine) {
                System.out.println("Game Over!");
                isFlagged = isFlaggedPermanent = true;
                // System.out.prinln("Play again?"); // do smth here
            }

            isOpen = true;
            text.setVisible(true);
            border.setFill(this.hasMine ? Color.DARKRED : Color.DARKGRAY);

            if (text.getText().isEmpty())
                getNeighbors(this).forEach(Tile::open);
        }

        public void flag() {
            if (isOpen)
                return;

            if (!isFlagged) {

                isFlagged = true;
                triesIncrease();
                if (!hasSuperMine) {
                    /* Usual case */
                    /* TODO: find a way to flag a tile graphically */
                } else if (tries <= 4) {
                    List<Tile> neighbors = getRowColNeighbors(this);
                    for (Tile tile : neighbors)
                        if (tile.hasMine) tile.flagPermanent();
                        else              tile.open();
                }

            } else if (!isFlaggedPermanent) {
                isFlagged = false;
            }
        }

        private void flagPermanent() {
            isFlagged = true;
            isFlaggedPermanent = true;
        }
    }


    private static int toInteger(String str) throws NumberFormatException {
        return Integer.parseInt(str);
    }
    private static boolean isValidCoordinate(int x, int y) {
        return (x >= 0 && x < X_TILES) && (y >= 0 && y < Y_TILES);
    }
    private static boolean invalidValue(int diff, int number, String type) {
        if (type.equals("mines"))
            return diff == 1 && (number < 9 || number > 11) ||
                   diff == 2 && (number < 35 || number > 45);
        else if (type.equals("seconds"))
            return diff == 1 && (number < 120 || number > 180) ||
                   diff == 2 && (number < 240 || number > 360);
        else
            return !(number == 0 || (diff != 1 && number == 1));
    }

    private void checkScenarioFile(String fileName) throws IOException {

        try {
            File scenarioId = new File("./src/main/java/com/example/minesweeper/" + fileName);
            if (!scenarioId.exists())
                throw new FileNotFoundException("File " + fileName + " does not exist.");

            Scanner myReader = new Scanner(scenarioId);
            for (int line = 0, difficulty = 0, data; myReader.hasNextLine(); ++line) {
                data = toInteger(myReader.nextLine());

                switch (line) {
                    case 0 -> {
                        if (data != 1 && data != 2)
                            throw new InvalidValueException("Invalid difficulty given. Must be either 1 or 2 (easy or difficult).");

                        difficulty = data;

                        X_TILES = Y_TILES = (difficulty == 1 ? 9 : 16);
                        grid = new Tile[X_TILES][Y_TILES];
                        WINDOW_WIDTH = TILE_SIZE * X_TILES;
                        WINDOW_HEIGHT = TILE_SIZE * Y_TILES + 80;
                    }
                    case 1 -> {
                        if (invalidValue(difficulty, data, "mines"))
                            throw new InvalidValueException("Invalid number of mines given.");
                        totalNumberOfMines = data;
                    }
                    case 2 -> {
                        if (invalidValue(difficulty, data, "seconds"))
                            throw new InvalidValueException("Invalid number of seconds given.");
                        numberOfSeconds = data;
                    }
                    case 3 -> {
                        if (invalidValue(difficulty, data, "supermine"))
                            throw new InvalidValueException(data != 0 && data != 1 ?
                                "Invalid data given about the existence of supermine. Must be either 0 or 1." :
                                "There can't be a supermine in easy difficulty."
                            );
                        superMineExists = (data == 1);
                    }
                    default -> throw new InvalidDescriptionException("The " + fileName + " file has excess lines.");
                }

                System.out.println(data);
            }
            myReader.close();

        } catch (InvalidValueException | InvalidDescriptionException e) {
            boolean isInvalidValueException = (e.getClass() == InvalidValueException.class);
            System.err.println("ERROR: The SCENARIO-ID.txt file contains wrong information.");
            e.printStackTrace();
            System.exit(isInvalidValueException ? 1 : 2);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: File contains non-integer values.");
            System.exit(3);
        }
    }


    private List<Tile> getNeighbors(Tile tile) {
        List<Tile> neighbors = new ArrayList<>();

        int[][] points = new int[][] {
                {-1, -1}, { 0, -1}, { 1, -1},
                {-1,  0},           { 1,  0},
                {-1,  1}, { 0,  1}, { 1,  1}
        };

        for (int[] point : points) {
            int dx = point[0];
            int dy = point[1];

            int neighborX = tile.x + dx;
            int neighborY = tile.y + dy;

            if (isValidCoordinate(neighborX, neighborY))
                neighbors.add(grid[neighborX][neighborY]);
        }

        return neighbors;
    }
    private List<Tile> getRowColNeighbors (Tile tile) {
        List<Tile> neighbors = new ArrayList<>();

        for (int x = 0; x < X_TILES; ++x)
            if (x != tile.x)
                neighbors.add(grid[x][tile.y]);

        for (int y = 0; y < Y_TILES; ++y)
            if (y != tile.y)
                neighbors.add(grid[tile.x][y]);

        return neighbors;
    }

    private void createContent() {
        /*BorderPane root = new BorderPane();
        MenuBar menuBar = new MenuBar();
        Pane paneCenter = new Pane();

        final Menu applicationMenu = new Menu("Application");
        final MenuItem createItem = new MenuItem("Create");
        final MenuItem loadItem = new MenuItem("Load");
        final MenuItem startItem = new MenuItem("Start");
        final SeparatorMenuItem separator = new SeparatorMenuItem();
        final MenuItem exitItem = new MenuItem("Exit");
        applicationMenu.getItems().addAll(createItem, loadItem, startItem, separator, exitItem);

        final Menu detailsMenu = new Menu("Details");
        final MenuItem roundsItem = new MenuItem("Rounds");
        final MenuItem solutionItem = new MenuItem("Solution");
        detailsMenu.getItems().addAll(roundsItem, solutionItem);

        menuBar.getMenus().addAll(applicationMenu, detailsMenu);

        root.setTop(menuBar);

        Button button = new Button("☺");
        button.relocate((((double) (X_TILES * TILE_SIZE)) / 2.0), 26);*/

        // Create the mines map // TODO : this needs optimization
        // boolean plantMine;
        boolean superMinePlanted = false;
        // final double densityOfMines = ((double) totalNumberOfMines) / ((double) (X_TILES * Y_TILES));
        final int nthMineWithSuperMine = ((int) (Math.random() * 100)) % totalNumberOfMines;
        boolean[][] minesMap = new boolean[X_TILES][Y_TILES];
        int currentNumberOfMines = 0, superMineX = 0, superMineY = 0;
        for (int x = 0; x < X_TILES; ++x) for (int y = 0; y < Y_TILES; ++y)
            minesMap[x][y] = false;

        while (currentNumberOfMines < totalNumberOfMines) {
            int randomX = ((int) (Math.random() * 100)) % X_TILES;
            int randomY = ((int) (Math.random() * 100)) % Y_TILES;

            if (!minesMap[randomX][randomY]) {
                minesMap[randomX][randomY] = true;
                if (++currentNumberOfMines == nthMineWithSuperMine) {
                    superMineX = randomX;
                    superMineY = randomY;
                    superMinePlanted = true;
                }
            }
        }

        // Create the grid and place all the mines (and supermine)
        for (int x = 0; x < X_TILES; ++x) {
            for (int y = 0; y < Y_TILES; ++y) {
                Tile tile = new Tile(x, y, minesMap[x][y], (x == superMineX && y == superMineY));
                grid[x][y] = tile;
                // root.getChildren().add(tile);
            }
        }

        // Insert the number of neighboring mines of each tile
        for (int x = 0; x < X_TILES; ++x) {
            for (int y = 0; y < Y_TILES; ++y) {
                Tile tile = grid[x][y];

                if (tile.hasMine) continue;

                long mines = getNeighbors(tile).stream().filter(t -> t.hasMine).count();

                if (mines > 0) {
                    tile.text.setText(String.valueOf(mines));

                }
            }
        }
    }


    @Override
    public void start(Stage stage) {
        // scene = new Scene(createContent());
        try {

            checkScenarioFile("medialab\\scenario-1.txt");
            // checkScenarioFile("medialab\\examples\\invalid_range_example.txt");

            BorderPane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("minesweeper-view.fxml")));
            root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

            Pane paneTop = new Pane();
            paneTop.setPrefWidth(X_TILES * TILE_SIZE);
            paneTop.setPrefHeight(Y_TILES * TILE_SIZE);
            root.setTop(paneTop);

            Pane paneCenter = new Pane();
            paneCenter.setPrefWidth(X_TILES * TILE_SIZE);
            paneCenter.setPrefHeight(Y_TILES * TILE_SIZE + 80);
            root.setCenter(paneCenter);

            createContent();

            timer.scheduleAtFixedRate(task, 0, 1000);

            for (int x = 0; x < X_TILES; ++x) for (int y = 0; y < Y_TILES; ++y)
                paneCenter.getChildren().add(grid[x][y]);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("MediaLab Minesweeper");
            stage.show();

            stage.setOnCloseRequest(event -> {
                event.consume();
                exit(stage);
            });

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: File not found!");
            e.printStackTrace();
            System.exit(4);
        } catch (IOException e) {
            System.err.println("ERROR");
            e.printStackTrace();
            System.exit(5);
        }
    }
    public static void exit(Stage stage) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("You're about to exit!");
        alert.setContentText("Are you sure you want to exit Minesweeper?");

        // { orElseThrow() } == { isPresent() && get() }
        if (alert.showAndWait().orElseThrow() == ButtonType.OK) {
            System.out.println("You successfully exited!");
            timer.cancel();
            stage.close();
        }
    }

    public static void main(String[] args) { launch(); }
}
