package monitors;

import scraper.remixmp4.RemixMp4Scraper;

public class RemixMp4_Scraper_Monitor extends Monitor {
    
    public static void main(String[] args) {
        setTITLE("RemixMp4 Scraper");
        doAll(new RemixMp4Scraper());
    }
}

