package scraper.abstraction;

import java.util.List;

public interface ScraperInterface {
    
    default void beforeCheck() {
    }
    
    default void firstStageForCheck() {
    }
    
    default void afterFirstStage() {
    }
    
    default void beforeLogin() {
    }
    
    default void afterLogin() {
    }
    
    default void operationWithLinksAfterScrape(List<String> scrapedLinks) {
    }
    
    default void nextPage() {
    }
    
}
