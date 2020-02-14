package scraper.abstraction;

import java.util.List;

public interface ScraperInterface {
   
   default void beforeCheck() {
   }
   
   default void driverCreationStage() {
   }
   
   default void beforeLogin() {
   }
   
   default void afterLogin() {
   }
   
   default void afterDriverCreation() {
   }
   
   default void betweenStages() {
   }
   
   default void scrapingStage() {
      mainOperation();
   }
   
   default void operationWithLinksAfterScrape(List<String> scrapedLinks) {
   }
   
   default void mainOperation() {
   }
}
