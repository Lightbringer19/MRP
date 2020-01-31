package scraper.smashvision;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

import java.text.MessageFormat;
import java.util.List;

public class SmashVisionScraper extends Scraper {
   
   public SmashVisionScraper() {
      USERNAME = yamlConfig.getSmashvision_username();
      PASS = yamlConfig.getSmashvision_password();
      dateFormat = "MM/dd/yyyy";
      loginUrl = "https://www.smashvision.net/";
      nameFieldNavigator = By.xpath("/html/body/div[3]/form/div[2]/div[1]/div/input");
      passFieldNavigator = By.xpath("/html/body/div[3]/form/div[2]/div[2]/div/input");
      submitButtonNavigator = By.id("btnSubmit");
      downloaded = mongoControl.smashVisionDownloaded;
      releaseName = "SmashVision Videos";
   }
   
   public static void main(String[] args) {
      SmashVisionScraper smashVisionScraper = new SmashVisionScraper();
      smashVisionScraper.start();
   }
   
   @Override
   @SneakyThrows
   public void afterLoginStage() {
      Thread.sleep(5000);
      driver.findElement(By.id("btnRowCollapseMsg")).click();
      Thread.sleep(2000);
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      Element dateElement = Jsoup.parse(html)
        .select("td[class=footable-visible footable-last-column]").first();
      return dateElement.text().contains("Updated:")
        ? dateElement.select("div").get(1).text()
        : dateElement.select("div").first().text();
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("td[class=footable-visible footable-last-column]")
        .stream()
        .map(trackInfo -> trackInfo.text().contains("Updated:")
          ? trackInfo.select("div").get(1).text()
          : trackInfo.select("div").first().text())
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
      Elements trackInfos = Jsoup.parse(html).select("tbody>tr");
      for (Element trackInfo : trackInfos) {
         Element date = trackInfo
           .select("td[class=footable-visible footable-last-column]")
           .first();
         String trackDate = date.text().contains("Updated:")
           ? date.select("div").get(1).text()
           : date.select("div").first().text();
         if (trackDate.equals(downloadDate)) {
            String trackName =
              trackInfo.select("div[class=grid-text]").get(0).text() + " " +
                trackInfo.select("div[class=grid-text]").get(1).text();
            Elements downloadButtons = trackInfo.select("div[class=btn-group]>button");
            for (Element downloadButton : downloadButtons) {
               String videoId = MessageFormat.format(
                 "https://www.smashvision.net/Handlers/DownloadHandler.ashx?id={0}",
                 downloadButton.attr("id").replace("btn_", ""));
               scrapedLinks.add(videoId);
               System.out.println(videoId + " " + trackName);
            }
         }
      }
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      driver.findElement(By.cssSelector(".next > a:nth-child(1)")).click();
      Thread.sleep(3000);
   }
}
