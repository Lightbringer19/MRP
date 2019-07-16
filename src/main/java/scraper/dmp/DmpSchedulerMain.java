package scraper.dmp;

import lombok.SneakyThrows;

import java.util.Timer;
import java.util.TimerTask;

public class DmpSchedulerMain extends Thread {

    @Override
    public void run() {
        Timer timer = new Timer();
        TimerTask checkDMP = new TimerTask() {
            @Override
            @SneakyThrows
            public void run() {
                DmpDriver dmpScraper = new DmpDriver();
                dmpScraper.everything();
                String html = dmpScraper.driver.getPageSource();
                DmpScraper.checkForNewRelease(html);
                dmpScraper.quitDriver();
            }
        };
        long sec = 1000;
        long min = sec * 60;
        long hour = 60 * min;
        timer.schedule(checkDMP, 0, 12 * hour);
    }
}
