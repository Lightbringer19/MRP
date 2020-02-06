package monitors;

import scraper.mymp3pool.MyMp3PoolScraper;

public class MyMp3Pool_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("MyMp3Pool Scraper");
      doAll(new MyMp3PoolScraper());
   }
}

