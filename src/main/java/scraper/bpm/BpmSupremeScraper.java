package scraper.bpm;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import scraper.abstraction.Scraper;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public class BpmSupremeScraper extends Scraper implements BpmApiService {
   
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
   public void afterLoginStage() {
      urlToGet = "https://app.bpmsupreme.com/new-releases/classic/audio";
      driver.get(urlToGet);
      Thread.sleep(10_000);
   }
   
   @Override
   @SneakyThrows
   protected void mainOperation(String firstDate, String downloadDate) {
      logger.log("Downloading Music Release");
      scrapeAndDownloadRelease(firstDate, downloadDate, releaseName);
      // SCRAPE VIDEOS AND DOWNLOAD
      urlToGet = "https://app.bpmsupreme.com/new-releases/classic/video";
      driver.get(urlToGet);
      Thread.sleep(10_000);
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
         if (date.equals(downloadDate)) {
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
               String downloadUrl = MessageFormat.format(
                 pattern,
                 artist, title, tag.text());
               System.out.println(downloadUrl);
               scrapedLinks.add(downloadUrl);
               
              /* String trackId = tag.attr("id")
                 .replace("New Releases_media_tag_", "");
               String linkForApi = MessageFormat.format(
                 "https://api.bpmsupreme.com/v1.2/media/{0}/download?crate=false",
                 trackId);
               List<String> info = getDownloadInfo(linkForApi);
               if (info != null) {
                  String downloadUrl = info.get(0);
                  cookieForAPI = info.get(1);
                  String trackType = tag.text();
                  logger.log(
                    MessageFormat.format("{0} - {1} ({2}) | {3}",
                      artist, title, tag.text(), downloadUrl));
                  scrapedLinks.add(downloadUrl);
               }*/
            }
         }
      }
   }
   
   @Override
   public void operationWithLinksAfterScrape(List<String> scrapedLinks) {
      List<String> formattedLinks = scrapedLinks
        .stream()
        .map(url -> url.replaceAll(" ", "%20"))
        .collect(Collectors.toList());
      scrapedLinks.clear();
      scrapedLinks.addAll(formattedLinks);
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      WebElement element = driver.findElement(By.xpath("/html/body/div/div[2]/div/div[5]/div/div[2]/div/div/div[1]/div/div[2]/div/div/div[3]/ul"));
      List<WebElement> nextButtons = element.findElements(By.tagName("li"));
      nextButtons.get(nextButtons.size() - 2).click();
      Thread.sleep(10_000);
   }
}
