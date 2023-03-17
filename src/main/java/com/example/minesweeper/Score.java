package com.example.minesweeper;

public record Score(
        int minesTotalNumber, int tries, int timePlayedSeconds, boolean playerWon
) {}
