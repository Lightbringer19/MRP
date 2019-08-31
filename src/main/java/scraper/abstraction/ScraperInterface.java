package scraper.abstraction;

import java.util.List;

public interface ScraperInterface {
    
    default void beforeCheck() {
    }
    
    default void firstStageCheck() {
    }
    
    default void beforeLogin() {
    }
    
    default void afterLogin() {
    }
    
    default void afterFirstStage() {
    }
    
    default void betweenStages() {
    }
    
    default void secondStageCheck() {
    }
    
    default void operationWithLinksAfterScrape(List<String> scrapedLinks) {
    }
    
    default void nextPage() {
    }
    
}
