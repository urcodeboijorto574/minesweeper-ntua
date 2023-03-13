package com.example.minesweeper;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    private static String fileName = "medialab\\scenario-1.txt"; /*"medialab\\examples\\invalid_range_example.txt"*/
    private static Scene scene;
    private static BorderPane root;
    private static Pane paneTop, paneCenter;

    private void createRoot() {
        root = new BorderPane();

        root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        root.setMinSize(X_TILES * TILE_SIZE, Y_TILES * TILE_SIZE);

        root.setTop(paneTop);
        root.setCenter(paneCenter);
    }

    private void createPaneTop() {
        paneTop = new Pane();
        paneTop.setPrefSize(WINDOW_WIDTH, 80);
        paneTop.setMinSize(WINDOW_WIDTH, paneTop.getPrefHeight());
        createMenuBar();
        createMinesRemaining();
        createStartButton();
        createSecondsRemaining();
    }
    private final double menuBarHeight = 25.6;
    private void createMenuBar() {
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

        MenuBar menuBar = new MenuBar();
        menuBar.relocate(0, 0);
        menuBar.setPrefSize(WINDOW_WIDTH, menuBarHeight);
        menuBar.getMenus().addAll(applicationMenu, detailsMenu);

        paneTop.getChildren().add(menuBar);
    }
    private void createMinesRemaining() {
        Rectangle minesRemainingBox = new Rectangle(80, 40);

        minesRemainingBox.setStroke(Color.DARKGRAY);
        minesRemainingBox.setStrokeWidth(3);
        minesRemainingBox.setFill(Color.rgb(42,84,40));
        minesRemainingBox.relocate(
                WINDOW_WIDTH - 5 - minesRemainingBox.getWidth() - minesRemainingBox.getStrokeWidth(),
                (paneTop.getPrefHeight() + menuBarHeight - 40 - minesRemainingBox.getStrokeWidth()) / 2.0
        );

        minesRemainingText = new Text(String.valueOf(minesTotalNumber));
        minesRemainingText.setFont(Font.font("Arial"));
        minesRemainingText.setScaleX(2);
        minesRemainingText.setScaleY(2);
        minesRemainingText.relocate(
                minesRemainingBox.getLayoutX() + minesRemainingBox.getWidth() / 1.7,
                menuBarHeight + minesRemainingBox.getHeight() / 2.5 + 5
        );

        paneTop.getChildren().addAll(minesRemainingBox, minesRemainingText);
    }
    private void createSecondsRemaining() {
        Rectangle secondsRemainingBox = new Rectangle(80, 40);

        secondsRemainingBox.setStroke(Color.DARKGRAY);
        secondsRemainingBox.setStrokeWidth(3);
        secondsRemainingBox.setFill(Color.rgb(42,84,40));
        secondsRemainingBox.relocate(
                5,
                (paneTop.getPrefHeight() + menuBarHeight - 40 - secondsRemainingBox.getStrokeWidth()) / 2.0
        );

        secondsRemainingText = new Text(String.valueOf(secondsTotalNumber));
        secondsRemainingText.setFont(Font.font("Arial"));
        secondsRemainingText.setScaleX(2);
        secondsRemainingText.setScaleY(2);
        secondsRemainingText.relocate(
                secondsRemainingBox.getX() + secondsRemainingBox.getWidth() / 1.7,
                menuBarHeight + secondsRemainingBox.getHeight() / 2.5 + 5
        );

        paneTop.getChildren().addAll(secondsRemainingBox, secondsRemainingText);
    }
    private void createStartButton() {
        startButton = new Button("☺");
        startButton.setPrefSize(72, 52);
        startButton.setFont(Font.font("Arial", FontWeight.BOLD,25));
        startButton.setTextFill(Color.GREEN);
        startButton.relocate(
                (WINDOW_WIDTH - startButton.getPrefWidth()) / 2.0,
                (paneTop.getPrefHeight() + menuBarHeight - startButton.getPrefHeight()) / 2.0
        );

        paneTop.getChildren().add(startButton);
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

    private void createPaneCenter() {
        paneCenter = new Pane();

        paneCenter.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT - paneTop.getPrefHeight());
        paneCenter.setMinSize(WINDOW_WIDTH, WINDOW_HEIGHT - paneTop.getPrefHeight());

        //Create the mines map // TODO : this needs optimization
        //boolean plantMine;
        //boolean superMinePlanted = false;
        //final double densityOfMines = ((double) totalNumberOfMines) / ((double) (X_TILES * Y_TILES));
        int currentNumberOfMines = 0, superMineX = 0, superMineY = 0;
        final int nthMineWithSuperMine = ((int) (Math.random() * 100)) % minesTotalNumber;
        boolean[][] minesMap = new boolean[X_TILES][Y_TILES];
        for (int x = 0; x < X_TILES; ++x) for (int y = 0; y < Y_TILES; ++y)
            minesMap[x][y] = false;

        // Decide mines' locations on the grid
        while (currentNumberOfMines < minesTotalNumber) {
            int randomX = ((int) (Math.random() * 100)) % X_TILES;
            int randomY = ((int) (Math.random() * 100)) % Y_TILES;

            if (!minesMap[randomX][randomY]) {
                minesMap[randomX][randomY] = true;
                if (++currentNumberOfMines == nthMineWithSuperMine) {
                    superMineX = randomX;
                    superMineY = randomY;
                    //superMinePlanted = true;
                }
            }
        }

        // Create grid and place all mines
        for (int x = 0; x < X_TILES; ++x) {
            for (int y = 0; y < Y_TILES; ++y) {
                Tile tile = new Tile(x, y, minesMap[x][y], (x == superMineX && y == superMineY));
                grid[x][y] = tile;
                paneCenter.getChildren().add(grid[x][y]);
            }
        }

        // Add neighboring-mines-info in every tile
        for (int x = 0; x < X_TILES; ++x) {
            for (int y = 0; y < Y_TILES; ++y) {
                Tile tile = grid[x][y];

                if (tile.hasMine) continue;

                long mines = getNeighbors(tile).stream().filter(t -> t.hasMine).count();

                if (mines > 0) {
                    switch ((int) mines) {
                        case 1 -> tile.text.setFill(Color.BLUE);
                        case 2 -> tile.text.setFill(Color.GREEN);
                        case 3 -> tile.text.setFill(Color.RED);
                        case 4 -> tile.text.setFill(Color.PURPLE);
                        case 5 -> tile.text.setFill(Color.BROWN);
                        case 6 -> tile.text.setFill(Color.LIGHTGREY);
                        case 7 -> tile.text.setFill(Color.DARKRED);
                        case 8 -> tile.text.setFill(Color.BLACK);
                    }
                    tile.text.setText(String.valueOf(mines));
                    tile.text.setStrokeWidth(5);
                }
            }
        }
    }

    private static Tile[][] grid;
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
            if (!gameStarted) {
                timer.scheduleAtFixedRate(task, 0, 1000);
                gameStarted = true;
            }

            if (isOpen || gameFinished)
                return;

            isOpen = true;
            triesIncrease();
            text.setVisible(true);
            border.setFill(hasMine ? Color.DARKRED : Color.DARKGRAY);

            if (hasMine) {
                timer.cancel();
                gameFinished = true;
                System.out.println("Game Over!");

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Game Over!");
                alert.setHeaderText("You landed on a mine!");
                alert.setContentText("Try again?");

                if (alert.showAndWait().orElseThrow() == ButtonType.OK) {
                    gameStarted = gameFinished = false;
                    timer = new Timer();
                    task = new TimerTask() {
                        @Override
                        public void run() {
                            if (secondsRemainingNumber > 0) {
                                secondsRemainingText.setText(String.valueOf(--secondsRemainingNumber));
                                System.out.println(secondsRemainingNumber);
                            } else {
                                // TODO: fix what happens when timer reaches zero
                                System.out.println("Timer reached zero!");
                                timer.purge();
                                gameFinished = true;
                            }
                        }
                    };
                    try {
                        initializeVariables(fileName);
                    } catch (IOException e) {
                        System.err.println("ERROR");
                        e.printStackTrace();
                        System.exit(5);
                    }
                    createPaneTop();
                    createPaneCenter();
                    createRoot();
                    scene.setRoot(root);

                    return;
                }

            }


            if (text.getText().isEmpty())
                getNeighbors(this).forEach(Tile::open);
        }

        public void flag() {
            if (!gameStarted) {
                timer.scheduleAtFixedRate(task, 0, 1000);
                gameStarted = true;
            }

            if (isOpen || minesRemainingNumber == 0 && !isFlagged || gameFinished)
                return;

            if (!isFlagged) {
                isFlagged = true;
                minesRemainingText.setText(String.valueOf(--minesRemainingNumber));
                if (!hasSuperMine || tries > 4) {
                    border.setFill(Color.ORANGERED);
                } else {
                    List<Tile> sameRowColNeighbors = getRowColNeighbors(this);
                    for (Tile tile : sameRowColNeighbors)
                        if (tile.hasMine) tile.flagPermanent();
                        else              tile.open();
                }

            } else if (!isFlaggedPermanent) {
                minesRemainingText.setText(String.valueOf(++minesRemainingNumber));
                border.setFill(Color.BLACK);
                isFlagged = false;
            }
        }

        private void flagPermanent() {
            isFlagged = true;
            isFlaggedPermanent = true;
            border.setFill(Color.ORANGE);
        }
    }

    private static boolean gameStarted = false, gameFinished = false;
    private static int tries = 0;
    private static void triesIncrease() { ++tries; }

    private static int minesTotalNumber, minesRemainingNumber;
    private static Text minesRemainingText;
    private static boolean superMineExists;

    private static int secondsTotalNumber, secondsRemainingNumber;
    private static Text secondsRemainingText;
    private static Timer timer;
    private static TimerTask task;

    private static Button startButton;

    private void initializeVariables(String scenarioFile) throws IOException {

        try {
            File scenarioId = new File("./src/main/java/com/example/minesweeper/" + scenarioFile);
            if (!scenarioId.exists())
                throw new FileNotFoundException("File " + scenarioFile + " does not exist.");

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
                        minesTotalNumber = data;
                        minesRemainingNumber = minesTotalNumber;
                    }
                    case 2 -> {
                        if (invalidValue(difficulty, data, "seconds"))
                            throw new InvalidValueException("Invalid number of seconds given.");
                        secondsTotalNumber = data;
                        secondsRemainingNumber = secondsTotalNumber;
                    }
                    case 3 -> {
                        if (invalidValue(difficulty, data, "supermine"))
                            throw new InvalidValueException(data != 0 && data != 1 ?
                                "Invalid data given about the existence of supermine. Must be either 0 or 1." :
                                "There can't be a supermine in easy difficulty."
                            );
                        superMineExists = (data == 1);
                    }
                    default -> throw new InvalidDescriptionException("The " + scenarioFile + " file has excess lines.");
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

    @Override
    public void start(Stage stage) {
        try {
            //root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("minesweeper-view.fxml")));
            initializeVariables(fileName);

            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    if (secondsRemainingNumber > 0) {
                        secondsRemainingText.setText(String.valueOf(--secondsRemainingNumber));
                        System.out.println(secondsRemainingNumber);
                    } else {
                        // TODO: fix what happens when timer reaches zero
                        System.out.println("Timer reached zero!");
                        timer.purge();
                        gameFinished = true;
                    }
                }
            };

            createPaneTop();
            createPaneCenter();
            createRoot();
            scene = new Scene(root);

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
    public static void exit(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("You're about to exit!");
        alert.setContentText("Are you sure you want to exit Minesweeper?");

        if (alert.showAndWait().orElseThrow() == ButtonType.OK) {
            System.out.println("You successfully exited!");
            timer.cancel();
            stage.close();
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

    public static void main(String[] args) { launch(); }
}
