package com.example.minesweeper;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class CounterBox extends StackPane {
    protected double x = 0, y = 0;
    private static final double borderWidth = MinesweeperApp.paneTopHeight;
    private static final double borderHeight = borderWidth / 2.0;
    private static final double borderStrokeWidth = 3;
    protected int totalNumber;
    protected int remainingNumber;

    protected final Rectangle border = new Rectangle(borderWidth, borderHeight);
    protected final Text text = new Text();

    public CounterBox(int totalNumber) {
        this.totalNumber = totalNumber;
        this.remainingNumber = totalNumber;

        border.setStroke(Color.DARKGRAY);
        border.setStrokeWidth(borderStrokeWidth);
        border.setFill(Color.rgb(42, 84, 40));

        text.setText(String.valueOf(totalNumber));
        text.setFont(Font.font("Arial", MinesweeperApp.TILE_SIZE * 0.6));
        text.setTextAlignment(TextAlignment.CENTER);

        this.setVisible(true);

        getChildren().addAll(border, text);
    }

    public void positionBox(double x, double y) {
        this.x = x;
        this.y = y;
        relocate(x, y);
    }

    public static double getHeightMixed() {
        return borderStrokeWidth + borderHeight;
    }
    public static double getWidthMixed() {
        return borderStrokeWidth + borderWidth;
    }

    public int getTotalNumber() { return totalNumber; }
    public int getRemainingNumber() { return remainingNumber; }
    public int increaseRemainingNumber() { return ++remainingNumber; }
    public int decreaseRemainingNumber() { return --remainingNumber; }
    public void setText(int number) { text.setText(String.valueOf(number)); }
    public void setText(String message) {
        text.setFont(Font.font(text.getFont().getSize() / 1.7));
        text.setText(message);
    }
    public void setTextColor(Color color) { text.setFill(color); }
}
