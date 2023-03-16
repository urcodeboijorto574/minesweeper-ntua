package com.example.minesweeper;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class InfoBox extends StackPane {
    protected double x = 0, y = 0;
    private static final double borderWidth = MinesweeperApp.paneTopHeight;
    private static final double borderHeight = borderWidth / 2.0;
    private static final double borderStrokeWidth = 3;
    protected int totalNumber;
    protected int remainingNumber;

    protected final Rectangle border = new Rectangle(borderWidth, borderHeight);
    protected final Text text = new Text();

    public InfoBox(int totalNumber) {
        this.totalNumber = totalNumber;
        this.remainingNumber = totalNumber;

        border.setStroke(Color.DARKGRAY);
        border.setStrokeWidth(borderStrokeWidth);
        border.setFill(Color.rgb(42, 84, 40));

        text.setText(String.valueOf(totalNumber));
        text.setFont(Font.font("Arial"));
        text.setScaleX(2); text.setScaleY(2);
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
//    public void setTotalNumber(int totalNumber) { this.totalNumber = totalNumber; }
    public int getRemainingNumber() { return remainingNumber; }
//    public void setRemainingNumber(int remainingNumber) { this.remainingNumber = remainingNumber; }
    public int increaseRemainingNumber() { return ++remainingNumber; }
    public int decreaseRemainingNumber() { return --remainingNumber; }
//    public Text getText() { return text; }
    public void setText(int number) {
        text.setText(String.valueOf(number));
    }
}
