package monitors;

import scraper.dmp.DmpSchedulerMain;

public class Auto_DMP_Scraper_Monitor extends Monitor {

    public static void main(String[] args) {
        setTITLE("Auto DMP Scraper");
        doAll(new DmpSchedulerMain());
    }
}
