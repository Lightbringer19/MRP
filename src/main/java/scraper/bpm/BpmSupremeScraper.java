package scraper.bpm;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

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
   
      dateFormat = "MM/dd/yy";
      downloaded = mongoControl.bpmDownloaded;
      releaseName = "Bpm Supreme";
   }
   
   public static void main(String[] args) {
      BpmSupremeScraper bpmSupremeScraper = new BpmSupremeScraper();
      bpmSupremeScraper.run();
   }
   
   @Override
   @SneakyThrows
   public void afterFirstStage() {
      urlToGet = "https://www.bpmsupreme.com/store/newreleases/audio/classic/1";
      driver.get(urlToGet);
      Thread.sleep(10_000);
   }
   
   @Override
   @SneakyThrows
   protected void mainOperation(String firstDate, String downloadDate) {
      logger.log("Downloading Music Release");
      scrapeAndDownloadRelease(firstDate, downloadDate, releaseName);
      // SCRAPE VIDEOS AND DOWNLOAD
      urlToGet = "https://www.bpmsupreme.com/store/newreleases/video/classic/1";
      driver.get(urlToGet);
      Thread.sleep(10_000);
      logger.log("Looking for Video Release");
      scrapeAndDownloadRelease(firstDate, downloadDate,
        releaseName + " Videos");
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html).select("span[class=date ng-binding]").first().text();
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("span[class=date ng-binding]")
        .stream()
        .filter(date -> !date.text().equals(firstDate))
        .findFirst()
        .map(Element::text)
        .orElse(null);
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate,
                                    List<String> scrapedLinks) {
      Elements trackInfos = Jsoup.parse(html)
        .select("li[class=even updatedversion ng-scope]");
      for (Element trackInfo : trackInfos) {
         String date = trackInfo.select("span[class=date ng-binding]").text();
         if (date.equals(downloadDate)) {
            String title = trackInfo.select("div[class=title_box]>h3").first()
              .attr("title");
            Element trackTags = trackInfo.select("div[class=tag]").first();
            Elements trackDownloadInfos = trackTags.select("span");
            for (Element downloadInfo : trackDownloadInfos) {
               String trackId = downloadInfo.attr("id")
                 .replace("icon_download_", "");
               String linkForApi = "https://www.bpmsupreme.com/store/output_file/" + trackId;
               List<String> info = getDownloadInfo(linkForApi, "");
               String downloadUrl = info.get(0);
               cookieForAPI = info.get(1);
               String trackType = downloadInfo.text();
               logger.log(title + " (" + trackType + ") | "
                 + downloadUrl);
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
        .collect(Collectors.toList());
      scrapedLinks.clear();
      scrapedLinks.addAll(formattedLinks);
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      String currentUrl = driver.getCurrentUrl();
      Integer pageNumber =
        Integer.valueOf(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
      urlToGet = currentUrl.replace(pageNumber.toString(), String.valueOf(pageNumber + 1));
      driver.get(urlToGet);
      Thread.sleep(10_000);
   }
}
