package monitors;

import scraper.late_night_record_pool.LateNightRPScraper;

public class LNRP_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("Late Night Record Pool Scraper");
      doAll(new LateNightRPScraper());
   }
}
