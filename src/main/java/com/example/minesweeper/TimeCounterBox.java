package com.example.minesweeper;

import javafx.scene.paint.Color;

import java.util.Timer;
import java.util.TimerTask;

public class TimeCounterBox extends CounterBox {
    private final Timer timer = new Timer();
    private final TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (remainingNumber > 0)
                text.setText(String.valueOf(--remainingNumber));
            else {
                MinesweeperApp.gameFinished = true;
                MinesweeperApp.timeCounter.setText("GAME OVER");
                MinesweeperApp.timeCounter.setTextColor(Color.RED);
                timer.cancel();
                System.out.println("Timer reached zero.");
            }
        }
    };

    public TimeCounterBox(int secondsTotalNumber) { super(secondsTotalNumber); }

    public void timerStart() { timer.scheduleAtFixedRate(task, 0, 1000); }
    public void timerCancel() { timer.cancel(); }
}
