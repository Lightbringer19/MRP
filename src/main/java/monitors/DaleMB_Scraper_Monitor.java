package monitors;

import scraper.dalemasbajo.DaleMasBajoScraper;

public class DaleMB_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("DaleMasBajo Scraper");
      doAll(new DaleMasBajoScraper());
   }
}

