package monitors;

import scraper.stoped.crateconnect.CrateConnectScraper;

public class CrateConnect_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("Crate Connect Scraper");
      doAll(new CrateConnectScraper());
   }
}
