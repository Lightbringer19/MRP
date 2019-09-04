package monitors;

import scraper.avdistrict.AvDistrictScraper;

public class AvDistrict_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("Av District Scraper");
      doAll(new AvDistrictScraper());
   }
}
