package scraper.avdistrict;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

import java.util.List;

public class AvDistrictScraper extends Scraper implements AvDistrictApiService {
   public AvDistrictScraper() {
      USERNAME = yamlConfig.getAvdistrict_username();
      PASS = yamlConfig.getAvdistrict_password();
   
      loginUrl = "http://www.avdistrict.net/";
      nameFieldNavigator = By.id("email");
      passFieldNavigator = By.id("password");
      submitButtonNavigator = By.id("btnlogin");
   
      dateFormat = "MM/dd/yyyy";
      downloaded = mongoControl.avDistrictDownloaded;
      releaseName = "Av District Videos";
   }
   
   public static void main(String[] args) {
      AvDistrictScraper avDistrictScraper = new AvDistrictScraper();
      avDistrictScraper.start();
   }
   
   // @Override
   // @SneakyThrows
   // public void beforeLogin() {
   //    driver.findElement(By.cssSelector(".btn-primary")).click();
   //    Thread.sleep(1000);
   // }
   
   @SneakyThrows
   @Override
   public void afterDriverCreation() {
      Thread.sleep(5000);
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html).select("tbody>tr>td")
        .get(5).text();
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("tbody>tr")
        .stream()
        .map(trackInfo -> trackInfo.select("td").get(5).text())
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
      Elements trackInfos = Jsoup.parse(html).select("tbody>tr");
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("td").get(5).text();
         if (trackDate.equals(downloadDate)) {
            String trackName = trackInfo.select("td").get(1).text() +
              trackInfo.select("td").get(2).text();
            String videoId = trackInfo.select("td").get(0)
              .select("a").attr("data-videoid");
            System.out.println(videoId + " " + trackName);
            String downloadLink = getDownloadLink(videoId);
            scrapedLinks.add(downloadLink);
            System.out.println(trackName + " | " + downloadLink);
         }
      }
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      driver.findElement(By.cssSelector(".next > a:nth-child(1)")).click();
      Thread.sleep(5000);
   }
}
