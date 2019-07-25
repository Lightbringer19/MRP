package scraper.eighth_wonder.old;

import lombok.SneakyThrows;

import java.util.Timer;
import java.util.TimerTask;

public class EwSchedulerMain extends Thread {
    
    @Override
    public void run() {
        Timer timer = new Timer();
        EwDriver ewDriver = new EwDriver();
        TimerTask checkEw = new TimerTask() {
            @Override
            @SneakyThrows
            public void run() {
                ewDriver.ewCheck();
            }
        };
        long sec = 1000;
        long min = sec * 60;
        long hour = 60 * min;
        timer.schedule(checkEw, 0, 6 * hour);
    }
}
