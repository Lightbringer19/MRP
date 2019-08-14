package monitors;

import scraper.beatjunkies.BjScraper;

public class BeatJunkies_Monitor extends Monitor {
    
    public static void main(String[] args) {
        setTITLE("BeatJunkies Scraper");
        doAll(new BjScraper());
    }
}
