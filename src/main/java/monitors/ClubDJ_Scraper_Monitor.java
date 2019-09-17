package monitors;

import scraper.clubdj.ClubDJScraper;

public class ClubDJ_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("ClubDJ Videos Scraper");
      doAll(new ClubDJScraper());
   }
}
