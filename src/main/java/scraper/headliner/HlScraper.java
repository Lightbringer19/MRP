package scraper.headliner;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

import java.util.List;
import java.util.Optional;

public class HlScraper extends Scraper {
    
    public static void main(String[] args) {
        HlScraper hlScraper = new HlScraper();
        hlScraper.run();
    }
    
    public HlScraper() {
        USERNAME = yamlConfig.getHl_username();
        PASS = yamlConfig.getHl_password();
        dateFormat = "MM/dd/yyyy";
        loginUrl = "https://headlinermusicclub.com/hmcmembers/";
        nameFieldNavigator = By.id("user_login1");
        passFieldNavigator = By.id("user_pass1");
        submitButtonNavigator = By.id("wp-submit1");
        downloaded = mongoControl.hlDownloaded;
        releaseName = "Headliner Music Club";
    }
    
    @Override
    @SneakyThrows
    public void afterLogin() {
        Thread.sleep(1000);
    }
    
    @Override
    public String scrapeFirstDate(String html) {
        return Jsoup.parse(html).select("div[class=tracks_homepage]>div>div")
           .first().text()
           .replace("Added on ", "");
    }
    
    @Override
    public String previousDateOnThisPage(String html, String firstDate) {
        //noinspection OptionalGetWithoutIsPresent
        return Jsoup.parse(html)
           .select("div[class=date-added]")
           .stream()
           .map(nextDate -> nextDate.text().replace("Added on ", ""))
           .filter(dateFormatted -> !dateFormatted.equals(firstDate))
           .findFirst().get();
    }
    
    @Override
    public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
        Document document = Jsoup.parse(html);
        Elements dates = document.select("div[class=date-added]");
        String containerHtml = document.select("div[class=tracks_homepage]").html();
        int indexOfFirstDate = containerHtml.indexOf(downloadDate);
        
        Optional<String> dateAfterDownloadDate = dates.stream()
           .map(date -> date.text().replace("Added on ", ""))
           .filter(dateFormatted ->
              !dateFormatted.equals(downloadDate) && !dateFormatted.equals(firstDate))
           .findFirst();
        String htmlWithTracks;
        if (dateAfterDownloadDate.isPresent()) {
            int indexOfSecondDate = containerHtml.indexOf(dateAfterDownloadDate.get());
            htmlWithTracks = containerHtml.substring(indexOfFirstDate, indexOfSecondDate);
        } else {
            htmlWithTracks = containerHtml.substring(indexOfFirstDate);
        }
        
        Jsoup.parse(htmlWithTracks).select("li[class*=post-view load-tracks]").stream()
           .map(trackInfo ->
              trackInfo.select("div[class*=download-stars]>a").attr("href"))
           .forEach(scrapedLinks::add);
    }
    
    @Override
    public void nextPage() {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("page")) {
            driver.get("https://headlinermusicclub.com/welcome/page/3/");
        } else {
            driver.get("https://headlinermusicclub.com/welcome/page/2/");
        }
    }
    
    @Override
    public void operationWithLinksAfterScrape(List<String> scrapedLinks) {
        System.out.println(scrapedLinks.size());
        scrapedLinks.forEach(System.out::println);
    }
}
