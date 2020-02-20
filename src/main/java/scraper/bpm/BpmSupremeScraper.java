package scraper.bpm;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import scraper.abstraction.Scraper;

import java.util.ArrayList;
import java.util.List;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;

public class BpmSupremeScraper extends Scraper implements BpmApiService {
   
   private boolean scrapeAll = false;
   
   public BpmSupremeScraper() {
      USERNAME = yamlConfig.getBpm_username();
      PASS = yamlConfig.getBpm_password();
      loginUrl = "https://www.bpmsupreme.com/login";
      nameFieldNavigator = By.id("login-form-email");
      passFieldNavigator = By.id("login-form-password");
      submitButtonNavigator = By.tagName("button");
   
      dateFormat = "yyyy-MM-dd";
      downloaded = mongoControl.bpmDownloaded;
      releaseName = "Bpm Supreme";
   }
   
   public static void main(String[] args) {
      BpmSupremeScraper bpmSupremeScraper = new BpmSupremeScraper();
      bpmSupremeScraper.run();
   }
   
   @Override
   @SneakyThrows
   public void beforeLogin() {
      sleep(3_000);
   }
   
   @Override
   @SneakyThrows
   public void afterDriverCreation() {
      urlToGet = "https://app.bpmsupreme.com/new-releases/classic/audio";
      driver.get(urlToGet);
      sleep(10_000);
   }
   
   @Override
   public void scrapingStage() {
      mainOperation();
      scrapePlaylists();
   }
   
   @SneakyThrows
   private void scrapePlaylists() {
      scrapeAll = true;
      List<String> playlists = getPlaylists();
      scrapeAllPlaylists(playlists);
      scrapeAll = false;
   }
   
   private void scrapeAllPlaylists(List<String> playlists) {
      String plUrl = "https://app.bpmsupreme.com/playlists/";
      playlists.forEach(playlistName -> scrapeAndDownloadPlaylist(playlistName,
        plUrl + playlistName.toLowerCase().replaceAll(" ", "-")));
   }
   
   @NotNull
   private List<String> getPlaylists() throws InterruptedException {
      driver.get("https://app.bpmsupreme.com/playlists");
      sleep(10_000);
      return Jsoup.parse(getPageSource())
        .select("li[class=set]")
        .stream()
        .map(element -> element.select("div[class=set-title]").text())
        .collect(toList());
   }
   
   @Override
   @SneakyThrows
   public List<String> scrapePlaylist(String playListUrl) {
      driver.get(playListUrl);
      sleep(10_000);
      List<String> scrapedLinks = new ArrayList<>();
      scrapeAllLinksOnPage(getPageSource(), null, null, scrapedLinks);
      operationWithLinksAfterScrape(scrapedLinks);
      return scrapedLinks;
   }
   
   @Override
   @SneakyThrows
   protected void scrapeAndDownloadOperation(String firstDate, String downloadDate) {
      logger.log("Downloading Music Release");
      scrapeAndDownloadRelease(firstDate, downloadDate, releaseName);
      // SCRAPE VIDEOS AND DOWNLOAD
      urlToGet = "https://app.bpmsupreme.com/new-releases/classic/video";
      driver.get(urlToGet);
      sleep(10_000);
      logger.log("Looking for Video Release");
      scrapeAndDownloadRelease(firstDate, downloadDate,
        releaseName + " Videos");
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html)
        .select("div[class=col-created_at link]").first().text();
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("div[class=col-created_at link]")
        .stream()
        .map(Element::text)
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   @SneakyThrows
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate,
                                    List<String> scrapedLinks) {
      Elements trackInfos = Jsoup.parse(html)
        .select("div[class*=row-item-album]");
      for (Element trackInfo : trackInfos) {
         String date = trackInfo.select("div[class=col-created_at link]").text();
         if (date.equals(downloadDate) || scrapeAll) {
            String title = trackInfo.select("div[class=row-track-name]").first().text();
            String artist = trackInfo.select("div[class=row-artist]").first().text();
            Elements tags = trackInfo.select("div[class=row-tags]").first()
              .select("span[class=tag-link]");
            for (Element tag : tags) {
               // construct download url from artist, title and tag name
               String pattern = "https://av.bpmsupreme.com/audio/{0} - {1} ({2}).mp3?download";
               if (urlToGet.contains("video")) {
                  pattern = pattern.replace("/audio/", "/video/");
                  pattern = pattern.replace(".mp3", ".mp4");
               }
               String downloadUrl = format(pattern,
                 artist, title, tag.text());
               System.out.println(downloadUrl);
               scrapedLinks.add(downloadUrl);
            }
         }
      }
   }
   
   @Override
   public void operationWithLinksAfterScrape(List<String> scrapedLinks) {
      List<String> formattedLinks = scrapedLinks
        .stream()
        .map(url -> url.replaceAll(" ", "%20"))
        .collect(toList());
      scrapedLinks.clear();
      scrapedLinks.addAll(formattedLinks);
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      WebElement element = driver.findElement(By.cssSelector("input.page-input"));
      int value = Integer.parseInt(element.getAttribute("value"));
      element.sendKeys(Keys.chord(Keys.CONTROL, Keys.BACK_SPACE));
      element.sendKeys(String.valueOf(value + 1));
      element.sendKeys(Keys.ENTER);
      sleep(10_000);
   }
}
