package scraper.abstraction;

import java.util.List;

public interface ScrapingInterface {
   
   String scrapeFirstDate(String html);
   
   default String previousDateOnThisPage(String html, String firstDate) {
      return null;
   }
   
   default void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
   }
   
   default void nextPage() {
   }
   
   default List<String> scrapePlaylist(String playListUrl) {
      return null;
      
   }
}
