package scraper.beatjunkies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class BjScraper extends Scraper {
    
    private String htmlWithTracks;
    private List<String> dates;
    
    public static void main(String[] args) {
        BjScraper bjScraper = new BjScraper();
        bjScraper.run();
    }
    
    public BjScraper() {
        USERNAME = yamlConfig.getBj_username();
        PASS = yamlConfig.getBj_password();
        loginUrl = "https://beatjunkies.com/login";
        nameFieldNavigator = By.id("user_login");
        passFieldNavigator = By.id("user_pass");
        submitButtonNavigator = By.id("wp-submit");
        downloaded = mongoControl.bjDownloaded;
        dateFormat = "MMMM d, yyyy";
        releaseName = "Beatjunkies";
    
        loginAtFirstStage = false;
        urlForFirstStage = "https://beatjunkies.com/record-pool";
    }
    
    @Override
    public String scrapeFirstDate(String html) {
        Element trackContainer = Jsoup.parse(requireNonNull(html))
           .select("div[class=widget-content]").first();
    
        dates = trackContainer
           .textNodes().stream()
           .filter(textNode -> !textNode.isBlank())
           .map(textNode -> textNode.text().trim())
           .collect(Collectors.toList());
        
        String containerHtml = trackContainer.html();
        int indexOfFirstDate = containerHtml.indexOf(dates.get(1));
        int indexOfSecondDate = containerHtml.indexOf(dates.get(2));
        htmlWithTracks = containerHtml.substring(indexOfFirstDate, indexOfSecondDate);
        return dates.get(0);
    }
    
    @Override
    protected List<String> scrapeLinks(String firstDate, String downloadDate) {
        return Jsoup.parse(htmlWithTracks)
           .select("div[class*=widget-beats-play rpool]").stream()
           .map(trackInfo -> "https://beatjunkies.com" +
              trackInfo.select("a[href*=download]").first().attr("href"))
           .peek(logger::log)
           .collect(Collectors.toList());
    }
    
    @Override
    protected String getDownloadDate(String firstDate) {
        return dates.get(1);
    }
}
    
