package monitors;

import scraper.stoped.beatjunkies.BjScraper;

public class BeatJunkies_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("BeatJunkies Scraper");
      doAll(new BjScraper());
   }
}
