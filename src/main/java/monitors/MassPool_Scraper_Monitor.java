package monitors;

import scraper.masspool.MassPoolScraper;

public class MassPool_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("Mass Pool Scraper");
      doAll(new MassPoolScraper());
   }
}
