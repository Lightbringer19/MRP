package scraper.xxx.crack4djs;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import scraper.abstraction.Scraper;

import java.util.List;
import java.util.stream.IntStream;

public class Crack4DjsScraper extends Scraper {
   
   public Crack4DjsScraper() {
      USERNAME = yamlConfig.getCrack4djs_username();
      PASS = yamlConfig.getCrack4djs_password();
      loginUrl = "https://v2-beta.crooklynclan.net/login/user";
      nameFieldNavigator = By.id("inputEmail3");
      passFieldNavigator = By.id("inputPassword3");
      submitButtonNavigator = By.cssSelector(".submit-button-2");
   
      dateFormat = "MM/dd/yyyy";
      downloaded = mongoControl.crack4DjsDownloaded;
      releaseName = "Crack 4 DJs";
   }
   
   public static void main(String[] args) {
      Crack4DjsScraper crack4DjsScraper = new Crack4DjsScraper();
      crack4DjsScraper.run();
   }
   
   @Override
   @SneakyThrows
   public void afterFirstStage() {
      Thread.sleep(5_000);
      Actions action = new Actions(driver);
      action.sendKeys(driver.findElement(By.cssSelector(".maingo")), Keys.DOWN).build().perform();
      Thread.sleep(500);
      action.sendKeys(driver.findElement(By.cssSelector(".maingo")), Keys.DOWN).build().perform();
      Thread.sleep(2_000);
      action.sendKeys(driver.findElement(By.cssSelector(".maingo")), Keys.DOWN).build().perform();
      Thread.sleep(500);
      action.sendKeys(driver.findElement(By.cssSelector(".maingo")), Keys.DOWN).build().perform();
      Thread.sleep(10_000);
      //
      for (int i = 0; i < 3; i++) {
         IntStream.range(0, 6).forEach(j -> action.sendKeys(driver.findElement(By.cssSelector(".responsive-table-wrap")),
           Keys.PAGE_DOWN).build().perform());
         action.moveToElement(driver.findElement(By.cssSelector(".view-more"))).build().perform();
         Thread.sleep(1_000);
         action.click(driver.findElement(By.cssSelector(".view-more"))).build().perform();
         Thread.sleep(1_000);
      }
      // System.out.println(getPageSource());
      Thread.sleep(30_000);
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return null;
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return null;
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
   
   }
   
   @Override
   public void nextPage() {
   
   }
}
