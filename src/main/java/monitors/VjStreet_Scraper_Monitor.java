package monitors;

import scraper.vjstreet.VjStreetScraper;

public class VjStreet_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("Vj Street Scraper");
      doAll(new VjStreetScraper());
   }
}
