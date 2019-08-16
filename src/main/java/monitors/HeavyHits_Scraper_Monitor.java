package monitors;

import scraper.heavyhits.HeavyHitsScraper;

public class HeavyHits_Scraper_Monitor extends Monitor {
    
    public static void main(String[] args) {
        setTITLE("Heavy Hits Scraper");
        doAll(new HeavyHitsScraper());
    }
}

