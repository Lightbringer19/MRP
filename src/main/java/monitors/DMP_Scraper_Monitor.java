package monitors;

import scraper.dmp.DmpSchedulerMain;

public class DMP_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("DMP Scraper");
      doAll(new DmpSchedulerMain());
   }
}
