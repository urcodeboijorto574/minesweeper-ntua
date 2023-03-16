package com.example.minesweeper;

import java.util.Timer;
import java.util.TimerTask;

public class TimeCounterBox extends InfoBox {
    private final Timer timer = new Timer();
    private final TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (remainingNumber > 0) {
                text.setText(String.valueOf(--remainingNumber));
            } else {
                // TODO: create popup window when timer reaches zero
                System.out.println("Timer reached zero!");
                timer.cancel();
                MinesweeperApp.gameFinished = true;
            }
        }
    };

    public TimeCounterBox(int secondsTotalNumber) {
        super(secondsTotalNumber);
    }

    public void timerStart() {
        timer.scheduleAtFixedRate(task, 0, 1000);
    }
    public void timerCancel() { timer.cancel(); }
}
