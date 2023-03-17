package com.example.minesweeper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class MinesweeperApp extends Application {
    // Information about object sizes in main scene.
    public static final int TILE_SIZE = 35;
    private static int WINDOW_WIDTH, WINDOW_HEIGHT, X_TILES, Y_TILES;
    public static final double paneTopHeight = TILE_SIZE * (80.0d / 35.0d);
    private static final double menuBarHeight = 25.6;

    // Objects and methods used to create the main scene.
    private static Scene scene;
    private static BorderPane root = null;
    private static Pane paneTop = null, paneCenter = null;
    private static Parent createRoot() {
        if (root == null) {
            root = new BorderPane();
            root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            root.setMinSize(X_TILES * TILE_SIZE, Y_TILES * TILE_SIZE);
        }
        root.setTop(createPaneTop());
        root.getChildren().remove(paneCenter);
        root.setCenter(createPaneCenter());
        return root;
    }
    private static Pane createPaneTop() {
        if (paneTop == null) {
            paneTop = new Pane();
            paneTop.setStyle("-fx-background-color: darkslategray");
            paneTop.setPrefSize(WINDOW_WIDTH, paneTopHeight);
            paneTop.setMinSize(WINDOW_WIDTH, paneTopHeight);
        }

        MenuBar tempMenuBar = createMenuBar();
        if (tempMenuBar != null) paneTop.getChildren().add(tempMenuBar);
        Button tempRestartButton = createRestartButton();
        if (tempRestartButton != null) paneTop.getChildren().add(tempRestartButton);
        paneTop.getChildren().addAll(timeCounter, mineCounter);

        return paneTop;
    }
    private static MenuBar createMenuBar() {
        if (!paneTop.getChildren().isEmpty())
            return null;

        // Application Menu
        final Menu applicationMenu = new Menu("Application"); {
            final MenuItem createItem = new MenuItem("Create");
            createItem.setOnAction(event -> {
                // TODO: create popup window for create item
            });
            final MenuItem loadItem = new MenuItem("Load");
            loadItem.setOnAction(event -> {
                // TODO: create popup window for load item
                fileName = "something here.txt";
                try {
                    initializeVariables(fileName);
                } catch (IOException e) {
                    System.err.println("ERROR");
                    e.printStackTrace();
                    System.exit(5);
                }
            });
            final MenuItem startItem = new MenuItem("Start");
            startItem.setOnAction(event -> restartGame());
            final SeparatorMenuItem separator = new SeparatorMenuItem();
            final MenuItem exitItem = new MenuItem("Exit");
            exitItem.setOnAction(event -> {
                Alert exitAlert = new Alert(Alert.AlertType.CONFIRMATION);
                exitAlert.setTitle("Exit");
                exitAlert.setHeaderText("You're about to exit!");
                exitAlert.setContentText("Are you sure you want to exit Minesweeper?");

                if (exitAlert.showAndWait().orElseThrow() == ButtonType.OK) {
                    System.out.println("Exiting Minesweeper Application.");
                    System.exit(0);
                }
            });
            applicationMenu.getItems().addAll(createItem, loadItem, startItem, separator, exitItem);
        }

        // Details Menu
        final Menu detailsMenu = new Menu("Details"); {
            final MenuItem roundsItem = new MenuItem("Rounds");
            roundsItem.setOnAction(event -> {
                // TODO: create popup window for rounds item
            });
            final MenuItem solutionItem = new MenuItem("Solution");
            solutionItem.setOnAction(event -> {
                timeCounter.timerCancel();
                gameFinished = true;
                revealMines();
                saveScore();
            });
            detailsMenu.getItems().addAll(roundsItem, solutionItem);
        }

        // Menu Bar
        MenuBar menuBar = new MenuBar();
        menuBar.relocate(0, 0);
        menuBar.setPrefSize(WINDOW_WIDTH, menuBarHeight);
        menuBar.getMenus().addAll(applicationMenu, detailsMenu);

        return menuBar;
    }
    private static Button restartButton;
    private static Button createRestartButton() {
        paneTop.getChildren().remove(restartButton);
        restartButton = new Button("☺");
        restartButton.setPrefSize(TILE_SIZE * 2, TILE_SIZE * 1.48);
        restartButton.setFont(Font.font("Arial", FontWeight.BOLD,TILE_SIZE * 0.65));
        restartButton.setTextFill(Color.GREEN);
        restartButton.relocate(
                (WINDOW_WIDTH - restartButton.getPrefWidth()) / 2.0,
                (paneTopHeight + menuBarHeight - restartButton.getPrefHeight()) / 2.0
        );

        restartButton.setOnAction(event -> restartGame());

        return restartButton;
    }
    public static MineCounterBox mineCounter = null;
    public static TimeCounterBox timeCounter = null;
    private static Pane createPaneCenter() {
        paneCenter = new Pane();
        paneCenter.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT - paneTopHeight);
        paneCenter.setMinSize(WINDOW_WIDTH, WINDOW_HEIGHT - paneTopHeight);

        //Create the mines map // TODO_kinda : creating mines map needs optimization
        /*
        //boolean plantMine;
        //boolean superminePlanted = false;
        //final double densityOfMines = ((double) totalNumberOfMines) / ((double) (X_TILES * Y_TILES));
        */
        final int nthMineWithSupermine;
        //nthMineWithSupermine = Tile.supermineExists ? (((int) (Math.random() * 100)) % mineCounter.getTotalNumber()) : 0;
        nthMineWithSupermine = Tile.supermineExists ? 1 : 0;
        boolean[][] minesMap = new boolean[X_TILES][Y_TILES];
        for (int x = 0; x < X_TILES; ++x) for (int y = 0; y < Y_TILES; ++y)
            minesMap[x][y] = false;

        // Decide mines' locations on the grid
        int currentNumberOfMines = 0, supermineX = -1, supermineY = -1;
        try {
            File minesFile = new File("mines.txt");
            if (minesFile.createNewFile())
                System.out.print("File created: " + minesFile.getName() + ". ");
            System.out.println("Writing to " + minesFile.getName() + " file.");
            FileWriter fileWriter = new FileWriter("mines.txt");

            boolean isSupermine;
            int randomX, randomY;
            while (currentNumberOfMines < mineCounter.getTotalNumber()) {
                randomX = ((int) (Math.random() * 100)) % X_TILES;
                randomY = ((int) (Math.random() * 100)) % Y_TILES;

                if (!minesMap[randomX][randomY]) {
                    minesMap[randomX][randomY] = true;
                    isSupermine = (++currentNumberOfMines == nthMineWithSupermine);
                    if (isSupermine) {
                        supermineX = randomX;
                        supermineY = randomY;
                        //superminePlanted = true;
                    }

                    fileWriter.write(randomY + ", " + randomX + ", " + (isSupermine ? "1" : "0") + "\n");
                }
            }
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("ERROR");
            e.printStackTrace();
            System.exit(5);
        }

        // Create grid and place all mines
        paneCenter.getChildren().clear();
        for (int x = 0; x < X_TILES; ++x) {
            for (int y = 0; y < Y_TILES; ++y) {
                try {
                    paneCenter.getChildren().remove(grid[x][y]);
                    Tile tile = new Tile(x, y, TILE_SIZE, minesMap[x][y], (x == supermineX) && (y == supermineY));
                    grid[x][y] = tile;
                    paneCenter.getChildren().add(grid[x][y]);
                } catch (ContradictionException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    System.exit(7);
                }
            }
        }

        // Add neighboring-mines-info in every tile
        for (int x = 0; x < X_TILES; ++x) {
            for (int y = 0; y < Y_TILES; ++y) {
                Tile tile = grid[x][y];

                if (tile.getHasMine()) {
                    tile.setTextColor(Color.WHITE);
                    continue;
                }

                long mines = getNeighbors(tile).stream().filter(Tile::getHasMine).count();

                if (mines > 0) {
                    switch ((int) mines) {
                        case 1 -> tile.setTextColor(Color.BLUE);
                        case 2 -> tile.setTextColor(Color.GREEN);
                        case 3 -> tile.setTextColor(Color.RED);
                        case 4 -> tile.setTextColor(Color.PURPLE);
                        case 5 -> tile.setTextColor(Color.BROWN);
                        case 6 -> tile.setTextColor(Color.LIGHTGREY);
                        case 7 -> tile.setTextColor(Color.DARKRED);
                        case 8 -> tile.setTextColor(Color.BLACK);
                    }
                    tile.setText((int) mines);
                }
            }
        }

        return paneCenter;
    }
    private static Tile[][] grid;
    public static List<Tile> getNeighbors(Tile tile) {
        List<Tile> neighbors = new ArrayList<>();

        int[][] points = new int[][] {
                {-1, -1}, { 0, -1}, { 1, -1},
                {-1,  0},           { 1,  0},
                {-1,  1}, { 0,  1}, { 1,  1}
        };

        for (int[] point : points) {
            int dx = point[0];
            int dy = point[1];

            int neighborX = tile.getX() + dx;
            int neighborY = tile.getY() + dy;

            if (isValidCoordinate(neighborX, neighborY))
                neighbors.add(grid[neighborX][neighborY]);
        }

        return neighbors;
    }
    public static List<Tile> getRowColNeighbors(Tile tile) {
        List<Tile> neighbors = new ArrayList<>();

        for (int x = 0; x < X_TILES; ++x)
            if (x != tile.getX())
                neighbors.add(grid[x][tile.getY()]);

        for (int y = 0; y < Y_TILES; ++y)
            if (y != tile.getY())
                neighbors.add(grid[tile.getX()][y]);

        return neighbors;
    }
    private static void revealMines() {
        for (int x = 0; x < X_TILES; ++x) for (int y = 0; y < Y_TILES; ++y)
            if (grid[x][y].getHasMine())
                grid[x][y].reveal();
    }

    // Information and methods about the status of the game, the scores, etc.
    public static boolean gameStarted = false, gameFinished = false;
    private static int tries = 0;
    public static void triesIncrease() { ++tries; }
    public static int getTries() { return tries; }
    private static final List<Score> scoreTable = new ArrayList<>();
    public static void saveScore() {
        if (scoreTable.size() == 5)
            scoreTable.remove(4);
        scoreTable.add(0, new Score(
                mineCounter.getTotalNumber(),
                tries,
                timeCounter.getTotalNumber() - timeCounter.getRemainingNumber(),
                false
                )
        );
    }

    // Methods used for initializing the game variables (number of mines and seconds etc.).
    private static String fileName = "medialab\\scenario-2.txt";
    public static void initializeVariables(String scenarioFile) throws IOException {
        try {
            File scenarioId = new File("./" + scenarioFile);
            if (!scenarioId.exists())
                throw new FileNotFoundException("File " + scenarioFile + " does not exist.");

            Scanner myReader = new Scanner(scenarioId);
            for (int line = 0, difficulty = 0, data; myReader.hasNextLine(); ++line) {
                data = toInteger(myReader.nextLine());

                switch (line) {
                    case 0 -> {
                        if (data != 1 && data != 2)
                            throw new InvalidValueException(
                                    "Invalid difficulty given. Must be either 1 or 2 (easy or difficult)."
                            );

                        difficulty = data;

                        X_TILES = Y_TILES = (difficulty == 1 ? 9 : 16);
                        grid = new Tile[X_TILES][Y_TILES];
                        WINDOW_WIDTH = TILE_SIZE * X_TILES;
                        WINDOW_HEIGHT = TILE_SIZE * Y_TILES + (int) paneTopHeight;
                    }
                    case 1 -> {
                        if (invalidValue(difficulty, data, "mines"))
                            throw new InvalidValueException("Invalid number of mines given.");

                        if (mineCounter != null) {
                            paneTop.getChildren().remove(mineCounter);
                        }
                        mineCounter = new MineCounterBox(data);
                        mineCounter.positionBox(
                                WINDOW_WIDTH - CounterBox.getWidthMixed() - 5,
                                (paneTopHeight + menuBarHeight - CounterBox.getHeightMixed()) / 2.0
                        );
                    }
                    case 2 -> {
                        if (invalidValue(difficulty, data, "seconds"))
                            throw new InvalidValueException("Invalid number of seconds given.");

                        if (timeCounter != null) {
                            timeCounter.timerCancel();
                            paneTop.getChildren().remove(timeCounter);
                        }
                        timeCounter = new TimeCounterBox(data);
                        timeCounter.positionBox(
                                5,
                                (paneTopHeight + menuBarHeight - CounterBox.getHeightMixed()) / 2.0
                        );
                    }
                    case 3 -> {
                        if (invalidValue(difficulty, data, "supermine"))
                            throw new InvalidValueException(data != 0 && data != 1 ?
                                "Invalid data given about the existence of supermine. Must be either 0 or 1." :
                                "There can't be a supermine in easy difficulty."
                            );
                        Tile.supermineExists = (data == 1);
                    }
                    default -> throw new InvalidDescriptionException(
                            "The " + scenarioFile + " file has excess information."
                    );
                }

                // Print to standard output the scenario imported.
                {
                    String str = "";
                    switch (line) {
                        case 0 -> str = "--Scenario imported: \nDifficulty: ";
                        case 1 -> str = "Mines: ";
                        case 2 -> str = "Seconds: ";
                        case 3 -> str = "Supermines: ";
                    }
                    System.out.println(str + data);
                }
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
    // Extra methods for specific uses.
    private static int toInteger(String str) throws NumberFormatException {
        return Integer.parseInt(str);
    }
    private static boolean isValidCoordinate(int x, int y) {
        return (x >= 0 && x < X_TILES) && (y >= 0 && y < Y_TILES);
    }
    private static boolean invalidValue(int diff, int number, String type) {
        boolean result = false;
        switch (type) {
            case "mines" -> result =
                    diff == 1 && (number < 9 || number > 11) ||
                            diff == 2 && (number < 35 || number > 45);
            case "seconds" -> result =
                    diff == 1 && (number < 120 || number > 180) ||
                            diff == 2 && (number < 240 || number > 360);
            case "supermine" -> result =
                    !(number == 0 || (diff != 1 && number == 1));
            default -> {
                try {
                    throw new Exception("ERROR: Invalid string given as an argument.");
                } catch (Exception e) {
                    System.err.print(e.getMessage());
                    e.printStackTrace();
                    System.exit(10);
                }
            }
        }
        return result;
    }

    @Override
    public void start(Stage stage) {
        try {
            //root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("minesweeper-view.fxml")));
            initializeVariables(fileName);
            scene = new Scene(createRoot());
            stage.setTitle("MediaLab Minesweeper");
            stage.setScene(scene);
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
    public static void restartGame() {
        gameStarted = gameFinished = false;
        tries = 0;
        try {
            initializeVariables(fileName);
        } catch (IOException e) {
            System.err.println("ERROR");
            e.printStackTrace();
            System.exit(5);
        }

        scene.setRoot(createRoot());
    }
    public static void endBecauseTimeRanOut() {
        Platform.runLater(() -> {
            System.out.println("Timer reached zero!");
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Game Over!");
            alert.setHeaderText("Unfortunately, you ran out of time.");
            alert.setContentText("Play again?");
            if (alert.showAndWait().orElseThrow() == ButtonType.OK)
                MinesweeperApp.restartGame();
        });
    }
    public static void exit(Stage stage) {
        Alert exitAlert = new Alert(Alert.AlertType.CONFIRMATION);
        exitAlert.setTitle("Exit");
        exitAlert.setHeaderText("You're about to exit!");
        exitAlert.setContentText("Are you sure you want to exit Minesweeper?");

        if (exitAlert.showAndWait().orElseThrow() == ButtonType.OK) {
            timeCounter.timerCancel();
            stage.close();
        }
    }

    public static void main(String[] args) { launch(); }
}
