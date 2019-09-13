package monitors;

import scraper.smashvision.SmashVisionScraper;

public class SmashVision_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("Smash Vision Scraper");
      doAll(new SmashVisionScraper());
   }
}
