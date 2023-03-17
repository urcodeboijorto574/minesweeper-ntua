package com.example.minesweeper;

public class Score {
    private final int minesTotalNumber;
    private final int tries;
    private final int timePlayedSeconds;
    private final boolean playerWon;

    public Score(int minesTotalNumber, int tries, int timePlayedSeconds, boolean playerWon) {
        this.minesTotalNumber = minesTotalNumber;
        this.tries = tries;
        this.timePlayedSeconds = timePlayedSeconds;
        this.playerWon = playerWon;
    }

    public int getMinesTotalNumber() { return minesTotalNumber; }
    public int getTries() { return tries; }
    public int getTimePlayedSeconds() { return timePlayedSeconds; }
    public boolean getPlayerWon() { return playerWon; }

}
