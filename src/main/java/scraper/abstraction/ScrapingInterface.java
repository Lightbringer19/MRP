package scraper.abstraction;

import java.util.List;

public interface ScrapingInterface {
    
    String scrapeDateOnFirstPage(String html);
    
    default String previousDateOnThisPage(String html, String dateOnFirstPage) {
        return null;
    }
    
    default void scrapeAllLinksOnPage(String html, String dateToDownload, String dateOnFirstPage, List<String> scrapedLinks) {
    }
}
