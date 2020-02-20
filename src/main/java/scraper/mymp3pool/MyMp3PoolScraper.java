package scraper.mymp3pool;

import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyMp3PoolScraper extends Scraper {
   
   private Map<String, String> myMp3PoolPlaylistMap;
   private MyMp3PoolVideosScraper myMp3PoolVideosScraper;
   
   public MyMp3PoolScraper() {
      System.setProperty("jsse.enableSNIExtension", "false");
      USERNAME = yamlConfig.getMp3_pool_username();
      PASS = yamlConfig.getMp3_pool_password();
      dateFormat = "MM/dd/yyyy";
      loginUrl = "https://mp3poolonline.com/user/login";
      nameFieldNavigator = By.id("edit-name");
      passFieldNavigator = By.id("edit-pass");
      submitButtonNavigator = By.id("edit-submit");
      downloaded = mongoControl.mp3PoolDownloaded;
      releaseName = "MyMp3Pool";
      myMp3PoolVideosScraper = new MyMp3PoolVideosScraper();
      myMp3PoolPlaylistMap = yamlConfig.getMyMp3PoolPlaylistsMap();
   }
   
   public static void main(String[] args) {
      MyMp3PoolScraper myMp3PoolScraper = new MyMp3PoolScraper();
      myMp3PoolScraper.run();
   }
   
   @Override
   public void scrapingStage() {
      //scrape audio
      mainOperation();
      scrapeVideos();
      scrapeAllPlaylists();
   }
   
   public void scrapeVideos() {
      driver.get("https://mp3poolonline.com/videoview");
      myMp3PoolVideosScraper.setDriver(driver);
      myMp3PoolVideosScraper.mainOperation();
   }
   
   public void scrapeAllPlaylists() {
      myMp3PoolPlaylistMap
        .forEach(this::scrapeAndDownloadPlaylist);
   }
   
   @Override
   public List<String> scrapePlaylist(String playListUrl) {
      driver.get(playListUrl);
      List<String> scrapedLinks = new ArrayList<>();
      scrapeAllLinksOnPage(getPageSource(), null, null, scrapedLinks);
      operationWithLinksAfterScrape(scrapedLinks);
      return scrapedLinks;
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html)
        .select("div[class=innerPlayer1]").first()
        .select("p").first().text()
        .replace("Added On: ", "");
   }
   
   @Override
   public String previousDateOnThisPage(String html, String date) {
      return Jsoup.parse(html).select("div[class=innerPlayer1]")
        .stream()
        .map(release -> release.select("p").first().text()
          .replace("Added On: ", ""))
        .filter(releaseDate -> !releaseDate.equals(date))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
      Jsoup.parse(html)
        .select("div[class=innerPlayer1]")
        .stream()
        .filter(release -> downloadDate == null || release.select("p").first().text()
          .replace("Added On: ", "").equals(downloadDate))
        .forEach(release -> release.select("div>ul>li")
          .forEach(track -> {
             // if (!track.select("div[class=track-title]").text().equals("")) {
             // logger.log("Adding Track to Download List: "
             //   + track.select("div[class=track-title]").text());
             // }
             track.select("div[class=download2 sub-section]>a")
               .stream()
               .map(link -> link.attr("href"))
               .filter(downloadUrl -> downloadUrl.contains("download/"))
               .forEach(scrapedLinks::add);
          }));
   }
   
   @Override
   public void operationWithLinksAfterScrape(List<String> scrapedLinks) {
      List<String> duplicates = scrapedLinks.stream()
        .filter(scrapedLink -> scrapedLink.endsWith("/"))
        .collect(Collectors.toList());
      scrapedLinks.removeAll(duplicates);
   }
   
   @Override
   public void nextPage() {
      String currentUrl = driver.getCurrentUrl();
      if (currentUrl.contains("page")) {
         int pageNumber = Integer.parseInt(currentUrl.substring(currentUrl.indexOf("=") + 1));
         driver.get("https://mp3poolonline.com/viewadminaudio?page=" + (pageNumber + 1));
      } else {
         driver.get("https://mp3poolonline.com/viewadminaudio?page=1");
      }
   }
}
