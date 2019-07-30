package monitors;

import scraper.eighth_wonder.EwScraper;

public class EW_Scraper_Monitor extends Monitor {
    
    public static void main(String[] args) {
        setTITLE("8thW Scraper");
        doAll(new EwScraper());
    }
}
