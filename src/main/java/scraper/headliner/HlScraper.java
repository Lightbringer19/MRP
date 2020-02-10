package scraper.headliner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HlScraper extends Scraper {
   
   private Map<String, String> headlinerPlaylistMap;
   
   public HlScraper() {
      USERNAME = yamlConfig.getHl_username();
      PASS = yamlConfig.getHl_password();
      dateFormat = "MM/dd/yyyy";
      loginUrl = "https://headlinermusicclub.com/hmcmembers/";
      nameFieldNavigator = By.id("user_login1");
      passFieldNavigator = By.id("user_pass1");
      submitButtonNavigator = By.id("wp-submit1");
      downloaded = mongoControl.headlinerDownloaded;
      releaseName = "Headliner Music Club";
      headlinerPlaylistMap = yamlConfig.getHeadlinerPlaylistsMap();
      downloadFirstTimeScrapedPlaylist = false;
   }
   
   public static void main(String[] args) {
      HlScraper hlScraper = new HlScraper();
      hlScraper.run();
   }
   
   @Override
   public void afterLogin() {
      driver.get("https://headlinermusicclub.com/welcome/");
   }
   
   @Override
   public void scrapingStage() {
      //Scrape main page with tracks
      mainOperation();
      // Scrape all playlists
      playlistsScrape();
   }
   
   private void playlistsScrape() {
      //check if playlists updated
      // -> start scraping
      setCookieForAPI();
      //scrape and download each playlist
      headlinerPlaylistMap
        .forEach(this::scrapeAndDownloadPlaylist);
   }
   
   @Override
   public List<String> scrapePlaylist(String playListUrl) {
      driver.get(playListUrl);
      return scrapeLinksFromPage(getPageSource());
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html)
        .select("div[class=tracks_homepage]>div>div")
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
      //api download
     /* String template = "https://headlinermusicclub.com/?get_file={0}";
      Jsoup.parse(htmlWithTracks).select("li[class*=post-view load-tracks]")
        .forEach(trackInfo ->
          trackInfo.select("a:contains(download)").stream()
            .map(element -> MessageFormat.format(template, element.attr("data-file")))
            .forEach(scrapedLinks::add));*/
      scrapedLinks.addAll(scrapeLinksFromPage(htmlWithTracks));
   }
   
   private List<String> scrapeLinksFromPage(String html) {
      return Jsoup.parse(html).select("li[class*=post-view load-tracks]").stream()
        .map(trackInfo ->
          trackInfo.select("div[class*=download-stars]>a").attr("href"))
        .collect(Collectors.toList());
   }
   
   @Override
   public void nextPage() {
      String currentUrl = driver.getCurrentUrl();
      if (currentUrl.contains("page")) {
         int pageNumber =
           Integer.parseInt(currentUrl.substring(currentUrl.lastIndexOf("/") - 1)
             .replace("/", ""));
         driver.get("https://headlinermusicclub.com/welcome/page/" + (pageNumber + 1) + "/");
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
