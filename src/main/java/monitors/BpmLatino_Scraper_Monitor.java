package monitors;

import scraper.bpm.BpmLatinoScraper;

public class BpmLatino_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("Bpm Latino Scraper");
      doAll(new BpmLatinoScraper());
   }
}

