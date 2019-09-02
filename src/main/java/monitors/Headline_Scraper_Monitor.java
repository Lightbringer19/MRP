package monitors;

import scraper.headliner.HlScraper;

public class Headline_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("Headline Scraper");
      doAll(new HlScraper());
   }
}

