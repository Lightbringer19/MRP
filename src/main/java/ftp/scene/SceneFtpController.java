package ftp.scene;

import lombok.SneakyThrows;

import java.util.Timer;
import java.util.TimerTask;

public class SceneFtpController extends Thread {

    private SceneFTPManager sceneFTPManager = new SceneFTPManager();

    @Override
    public void run() {
        Timer timer = new Timer();
        TimerTask ftpCheckMp3 = new TimerTask() {
            @Override
            @SneakyThrows
            public void run() {
                sceneFTPManager.checkFtp("SCENE-MP3");
            }
        };
        TimerTask ftpCheckFlac = new TimerTask() {
            @Override
            @SneakyThrows
            public void run() {
                sceneFTPManager.checkFtp("SCENE-FLAC");
            }
        };

        long sec = 1000;
        long min = sec * 60;
        timer.schedule(ftpCheckMp3, 0, min);
        timer.schedule(ftpCheckFlac, 0, min);
    }
}
