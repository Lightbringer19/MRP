package scraper.abstraction;

import java.util.List;

public interface ScrapingInterface {
    
    String scrapeDate(String html);
    
    default String previousDateOnThisPage(String html, String date) {
        return null;
    }
    
    default void scrapeAllLinksOnPage(String html, String date, List<String> scrapedLinks) {
    }
}
