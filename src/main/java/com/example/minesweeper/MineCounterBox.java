package com.example.minesweeper;

public class MineCounterBox extends CounterBox {
    private final int tilesSafeNumber;
    private int tilesOpenNumber = 0;

    public MineCounterBox(int minesTotalNumber) {
        super(minesTotalNumber);
        text.setText(totalNumber + "/" + totalNumber);
        final int tilesTotalNumber = minesTotalNumber <= 11 ? 9 * 9 :  16 * 16;
        tilesSafeNumber = tilesTotalNumber - totalNumber;
    }

    public void increaseOpenedTiles() { ++tilesOpenNumber; }
    public int getTilesSafeNumber() { return tilesSafeNumber; }
    public int getTilesOpenNumber() { return tilesOpenNumber; }

    @Override
    public void setText(int number) {
        text.setText(number + "/" + totalNumber);
    }
}
