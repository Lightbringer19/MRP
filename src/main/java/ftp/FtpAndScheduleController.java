package ftp;

import lombok.SneakyThrows;

import java.util.Timer;
import java.util.TimerTask;

public class FtpAndScheduleController extends Thread {

    private FTPManager ftpManager = new FTPManager();
    private ScheduleWatcher scheduleWatcher = new ScheduleWatcher();

    @Override
    public void run() {
        Timer timer = new Timer();
        TimerTask ftpCheck = new TimerTask() {
            @Override
            @SneakyThrows
            public void run() {
                ftpManager.checkFtp("RECORDPOOL");
            }
        };
        TimerTask ftpCheckBeat = new TimerTask() {
            @Override
            @SneakyThrows
            public void run() {
                ftpManager.checkFtp("BEATPORT");
            }
        };
        TimerTask scheduleDownloaded = new TimerTask() {
            @Override
            @SneakyThrows
            public void run() {
                scheduleWatcher.scheduleDownloaded();
            }
        };
        TimerTask addReleaseToTagQue = new TimerTask() {
            @Override
            @SneakyThrows
            public void run() {
                scheduleWatcher.addReleaseToTagQue();
            }
        };
        TimerTask checkToDownloadQueue = new TimerTask() {
            @Override
            @SneakyThrows
            public void run() {
                scheduleWatcher.checkToDownloadQueue();
            }
        };
        long sec = 1000;
        long min = sec * 60;
        long hour = 60 * min;
        timer.schedule(ftpCheck, 0, 2 * hour);
        timer.schedule(ftpCheckBeat, 0, min);
        timer.schedule(checkToDownloadQueue, 0, 2 * hour);
        timer.schedule(scheduleDownloaded, 0, min);
        timer.schedule(addReleaseToTagQue, 0, min);
    }

    public static void main(String[] args) {
        new FtpAndScheduleController().run();
    }

}
