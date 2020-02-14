package scraper.stoped.vjstreet;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

import java.util.List;

public class VjStreetScraper extends Scraper implements VjStreetApiService {
   
   public VjStreetScraper() {
      USERNAME = yamlConfig.getVjstreet_username();
      PASS = yamlConfig.getVjstreet_password();
      dateFormat = "MM/dd/yyyy";
      loginUrl = "https://www.vjstreet.com/en";
      nameFieldNavigator = By.xpath("/html/body/div[1]/div[1]/nav/div[2]/div/div[2]/ul[2]/li[3]/div/form/input[1]");
      passFieldNavigator = By.xpath("/html/body/div[1]/div[1]/nav/div[2]/div/div[2]/ul[2]/li[3]/div/form/input[2]");
      submitButtonNavigator = By.xpath("/html/body/div[1]/div[1]/nav/div[2]/div/div[2]/ul[2]/li[3]/div/form/input[3]");
      downloaded = mongoControl.vjStreetDownloaded;
      releaseName = "VJ Street Videos";
   }
   
   public static void main(String[] args) {
      VjStreetScraper vjStreetScraper = new VjStreetScraper();
      vjStreetScraper.start();
   }
   
   @Override
   @SneakyThrows
   public void beforeLogin() {
      sleep(2000);
      driver.findElement(By.xpath("/html/body/div[10]/div/div/div[3]/button[2]")).click();
      sleep(2000);
      driver.findElement(By.id("login-box-button")).click();
   }
   
   @SneakyThrows
   @Override
   public void afterDriverCreation() {
      driver.get("https://www.vjstreet.com/en/catalog");
      sleep(2000);
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html).select("tbody>tr").first()
        .select("td").first().text();
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("tbody>tr")
        .stream()
        .map(trackInfo -> trackInfo.select("td").first().text())
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
      Elements trackInfos = Jsoup.parse(html).select("tbody>tr");
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("td").first().text();
         if (trackDate.equals(downloadDate)) {
            for (Element downloadInfo : trackInfo
              .select("td[class=catalog-download hidden-xs]>a")) {
               String trackName = downloadInfo.attr("title");
               String downloadCode = downloadInfo.attr("data-code");
               String downloadUrl = downloadInfo.attr("href");
               buyTrack(downloadCode);
               scrapedLinks.add(downloadUrl);
               System.out.println(downloadUrl + " " + trackName);
            }
         }
      }
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      String currentUrl = driver.getCurrentUrl();
      String pageNumber = currentUrl.substring(currentUrl.lastIndexOf("/") + 1);
      String newLink;
      if (!pageNumber.matches("^[0-9]+$")) {
         newLink = currentUrl + "/" + 50;
      } else {
         newLink = currentUrl.replace(pageNumber,
           String.valueOf(Integer.parseInt(pageNumber) + 50));
      }
      driver.get(newLink);
      sleep(500);
   }
}
