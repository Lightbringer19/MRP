package monitors;

import scraper.bpm.BpmSupreme;

public class BpmSupreme_Scraper_Monitor extends Monitor {
    
    public static void main(String[] args) {
        setTITLE("BpmSupreme Scraper");
        doAll(new BpmSupreme());
    }
}
