package scraper.mp3pool;

import lombok.SneakyThrows;

import java.util.Timer;
import java.util.TimerTask;

public class Mp3PoolSchedulerMain extends Thread {

    @Override
    public void run() {
        Timer timer = new Timer();
        Mp3poolDriver mp3poolDriver = new Mp3poolDriver();
        TimerTask checkEw = new TimerTask() {
            @Override
            @SneakyThrows
            public void run() {
                mp3poolDriver.check();
            }
        };
        long sec = 1000;
        long min = sec * 60;
        long hour = 60 * min;
        timer.schedule(checkEw, 0, 6 * hour);
    }
}
