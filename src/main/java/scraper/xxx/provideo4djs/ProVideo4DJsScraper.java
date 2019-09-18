package scraper.xxx.provideo4djs;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import scraper.abstraction.Scraper;

import java.util.List;

public class ProVideo4DJsScraper extends Scraper {
   
   private int page = 1;
   
   public ProVideo4DJsScraper() {
   
      USERNAME = yamlConfig.getProvideos4djs_username();
      PASS = yamlConfig.getProvideos4djs_password();
      dateFormat = "yyyy-MM-dd";
      loginUrl = "https://www.provideos4djs.com/index.php";
      nameFieldNavigator = By.id("username");
      passFieldNavigator = By.id("password");
      submitButtonNavigator = By.xpath("/html/body/div[2]/div/section/aside/div/form/table/tbody/tr[3]/td/input");
      downloaded = mongoControl.proVideo4DJsDownloaded;
      releaseName = "ProVideo4DJs Videos";
      
   }
   
   public static void main(String[] args) {
      ProVideo4DJsScraper proVideo4DJsScraper = new ProVideo4DJsScraper();
      proVideo4DJsScraper.start();
   }
   
   @Override
   @SneakyThrows
   public void beforeLogin() {
      sleep(2000);
      driver.findElement(By.xpath("/html/body/div[6]/table/tbody/tr/td[1]/img")).click();
      sleep(2000);
   }
   
   @Override
   @SneakyThrows
   public void afterFirstStage() {
      driver.get("https://www.provideos4djs.com/index.php?action=downloads");
      sleep(2000);
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html).select("div[class=data]").first()
        .select("strong").first().text()
        .replace("New Version Added: ", "");
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("div[class=data]")
        .stream()
        .map(trackInfo -> trackInfo.select("strong").first().text()
          .replace("New Version Added: ", ""))
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
      Elements trackInfos = Jsoup.parse(html).select(
        "div[class=content_data_downloads]>div[id*=id]");
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("div[class=data]").first()
           .select("strong").first().text()
           .replace("New Version Added: ", "");
         if (trackDate.equals(downloadDate)) {
            String linkForRequest = "https://www.provideos4djs.com/" +
              trackInfo.select("a").first().attr("href");
            String trackName = trackInfo.select("img").first().attr("alt");
            String downloadUrl = getLocation(linkForRequest);
            scrapedLinks.add(downloadUrl);
            System.out.println(downloadUrl + " " + trackName);
         }
      }
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      page++;
      Actions action = new Actions(driver);
      for (int i = 0; i < 20; i++) {
         action.sendKeys(Keys.PAGE_DOWN).build().perform();
      }
      action.sendKeys(driver.findElement(By.id("goToPage")), Keys.BACK_SPACE).build().perform();
      action.sendKeys(driver.findElement(By.id("goToPage")), Keys.DELETE).build().perform();
      driver.findElement(By.id("goToPage")).sendKeys(String.valueOf(page));
      driver.findElement(By.xpath("/html/body/div[2]/div/section/div/div[51]/span/input[1]")).click();
      sleep(5000);
   }
}
