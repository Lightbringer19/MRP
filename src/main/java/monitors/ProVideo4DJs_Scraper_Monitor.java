package monitors;

import scraper.provideo4djs.ProVideo4DJsScraper;

public class ProVideo4DJs_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("ProVideo4DJs Scraper");
      doAll(new ProVideo4DJsScraper());
   }
}
