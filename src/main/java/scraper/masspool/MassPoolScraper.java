package scraper.masspool;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import scraper.abstraction.Scraper;

import java.text.MessageFormat;
import java.util.List;

public class MassPoolScraper extends Scraper {
   public MassPoolScraper() {
      USERNAME = yamlConfig.getMasspool_username();
      PASS = yamlConfig.getMasspool_password();
   
      loginUrl = "https://www.masspoolmp3.com/Secure/MainLogin.aspx";
      nameFieldNavigator = By.id("Content_txtUsername");
      passFieldNavigator = By.id("Content_txtPassword");
      submitButtonNavigator = By.id("Content_cmdSubmit");
   
      dateFormat = "MM-dd-yyyy";
      releaseName = "Mass Pool";
   }
   
   public static void main(String[] args) {
      MassPoolScraper massPoolScraper = new MassPoolScraper();
      massPoolScraper.start();
   }
   
   @Override
   @SneakyThrows
   public void afterLoginStage() {
      driver.get("http://www.masspoolmp3.com/members/downloads/Dance");
      Actions action = new Actions(driver);
      WebElement choiceMenu = driver.findElement(
        By.cssSelector("#dl_table_length > label:nth-child(1) > select:nth-child(1)"));
      choiceMenu.click();
      for (int i = 0; i < 5; i++) {
         Thread.sleep(500);
         action.sendKeys(choiceMenu, Keys.DOWN).build().perform();
      }
   }
   
   @Override
   // @SneakyThrows
   public void scrapingStage() {
      downloadCategory("Dance");
      driver.get("http://www.masspoolmp3.com/members/downloads/HipHop");
      downloadCategory("Hip-Hop");
      driver.get("http://www.masspoolmp3.com/members/downloads/Underground");
      downloadCategory("Underground");
      // sleep(666666);
      driver.get("http://www.masspoolmp3.com/members/downloads/Trance");
      downloadCategory("Trance-Tech");
      driver.get("http://www.masspoolmp3.com/members/downloads/Electro-House");
      downloadCategory("Electro-House");
      // TODO: 02.01.2020 ACTIVATE LATIN LATER
      // driver.get("http://www.masspoolmp3.com/members/downloads/Latin");
      // downloadCategory("Latin-International");
   }
   
   private void downloadCategory(String category) {
      downloaded = mongoControl.poolsDB.getCollection(
        "masspool_" + category.replaceAll("-", ""));
      releaseName = "Mass Pool " + category;
      logger.log("Scraping: " + category);
      fullScrape();
      logger.log("Scraped: " + category);
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html)
        .select("table[id=dl_table]>tbody>tr").first()
        .select("td").get(2)
        .text();
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("table[id=dl_table]>tbody>tr")
        .stream()
        .map(element -> element.select("td").get(2).text())
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
      Elements trackInfos = Jsoup.parse(html).select("table[id=dl_table]>tbody>tr");
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("td").get(2).text();
         if (trackDate.equals(downloadDate)) {
            String trackName = trackInfo.select("td").get(1).text();
            String downloadPartLink = trackInfo.select("td").get(1)
              .select("a").attr("href");
            String downloadUrl = MessageFormat.format(
              "https://www.masspoolmp3.com" +
                "{0}", downloadPartLink);
            String downloadLink = getLocation(getLocation(downloadUrl));
            System.out.println(trackName + " | " + downloadLink);
            if (downloadLink != null) {
               scrapedLinks.add(downloadLink);
            }
         }
      }
   }
   
   @Override
   public void nextPage() {
      driver.findElement(By.id("dl_table_next")).click();
   }
}
