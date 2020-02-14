package scraper.bpm;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import scraper.abstraction.Scraper;

import java.util.List;

import static java.text.MessageFormat.format;

public class BpmLatinoScraper extends Scraper implements BpmApiService {
   
   private boolean audioRelease;
   
   public BpmLatinoScraper() {
      USERNAME = yamlConfig.getBpm_latino_username();
      PASS = yamlConfig.getBpm_latino_password();
      loginUrl = "https://bpmlatino.com/login";
      nameFieldNavigator = By.id("login-form-email");
      passFieldNavigator = By.id("login-form-password");
      submitButtonNavigator = By.xpath("/html/body/main/div/form/footer");
   
      dateFormat = "yyyy-MM-dd";
      downloaded = mongoControl.bpmLatinoDownloaded;
      releaseName = "Bpm Supreme Latino";
   
      audioRelease = true;
   }
   
   public static void main(String[] args) {
      BpmLatinoScraper bpmLatinoScraper = new BpmLatinoScraper();
      bpmLatinoScraper.run();
   }
   
   @Override
   @SneakyThrows
   public void beforeLogin() {
      sleep(2_000);
      
   }
   
   @Override
   @SneakyThrows
   public void afterDriverCreation() {
      urlToGet = "https://app.bpmlatino.com/new-releases/classic/audio";
      driver.get(urlToGet);
      Thread.sleep(10_000);
   }
   
   @Override
   @SneakyThrows
   protected void scrapeAndDownloadOperation(String firstDate, String downloadDate) {
      logger.log("Downloading Music Release");
      audioRelease = true;
      scrapeAndDownloadRelease(firstDate, downloadDate, releaseName);
      // SCRAPE VIDEOS AND DOWNLOAD
      urlToGet = "https://app.bpmlatino.com/new-releases/classic/video";
      driver.get(urlToGet);
      Thread.sleep(10_000);
      logger.log("Looking for Video Release");
      audioRelease = false;
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
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
      String cssQuery = "div[class=row-item row-item-album audio ]";
      if (!audioRelease) {
         cssQuery = "div[class=row-item  row-item-album video ]";
      }
      Elements trackInfos = Jsoup.parse(html).select(
        cssQuery);
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("div[class=col-created_at link]").first()
           .text();
         if (trackDate.equals(downloadDate)) {
            String title = trackInfo.select("div[class=row-track]").text();
            Elements tags = trackInfo.select("div[class=row-tags]>span");
            for (Element tag : tags) {
               String trackId = tag.attr("id").replace("New Releases_media_tag_", "");
               String linkForApi = format(
                 "https://api.bpmlatino.com/v1/media/{0}/download?crate=false", trackId);
               List<String> info = getDownloadInfo(linkForApi);
               String downloadUrl = info.get(0);
               cookieForAPI = info.get(1);
               String trackType = tag.text();
               System.out.println(title + " (" + trackType + ") | "
                 + downloadUrl);
               scrapedLinks.add(downloadUrl);
            }
         }
      }
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      WebElement element = driver.findElement(By.cssSelector("input.page-input"));
      int value = Integer.parseInt(element.getAttribute("value"));
      element.sendKeys(Keys.chord(Keys.CONTROL, Keys.BACK_SPACE));
      element.sendKeys(String.valueOf(value + 1));
      element.sendKeys(Keys.ENTER);
      Thread.sleep(10_000);
   }
}
