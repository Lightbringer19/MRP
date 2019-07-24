package scraper.abstraction;

import java.util.List;

public interface ScrapeInterface {
    
    String scrapeDate(String html);
    
    String previousDateOnThisPage(String html, String date);
    
    void scrapeAllLinksOnPage(String html, String date, List<String> scrapedLinks);
}
