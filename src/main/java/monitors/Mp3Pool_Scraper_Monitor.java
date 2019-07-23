package monitors;

import scraper.mp3pool.Mp3PoolSchedulerMain;

public class Mp3Pool_Scraper_Monitor extends Monitor {

    public static void main(String[] args) {
        setTITLE("Mp3Pool Scraper");
        doAll(new Mp3PoolSchedulerMain());
    }
}

