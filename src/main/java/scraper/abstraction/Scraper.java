package scraper.abstraction;

import com.mongodb.client.MongoCollection;
import configuration.YamlConfig;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import mongodb.MongoControl;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import utils.Constants;
import utils.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static java.util.concurrent.ThreadLocalRandom.current;

public abstract class Scraper extends Thread
  implements ScrapingInterface, DownloadInterface, ScraperInterface {
   
   protected static MongoControl mongoControl;
   protected static String cookieForAPI;
   protected static YamlConfig.Config yamlConfig;
   protected static WebDriver driver;
   protected static FirefoxOptions firefoxOptions;
   
   protected String urlToGet;
   
   protected Logger logger;
   
   protected String dateFormat;
   
   protected String USERNAME;
   protected String PASS;
   
   protected String loginUrl;
   protected By nameFieldNavigator;
   protected By passFieldNavigator;
   protected By submitButtonNavigator;
   
   protected MongoCollection<Document> downloaded;
   protected String releaseName;
   
   protected String urlForFirstStage;
   protected boolean loginAtFirstStage = true;
   
   protected boolean exitAfterCheck = true;
   
   public Scraper() {
      yamlConfig = new YamlConfig().config;
      mongoControl = new MongoControl();
      System.setProperty("webdriver.gecko.driver", Constants.filesDir + "geckodriver.exe");
      FirefoxProfile ini = new ProfilesIni().getProfile("selenium");
      firefoxOptions = new FirefoxOptions().setProfile(ini);
   }
   
   @Override
   @SuppressWarnings("Duplicates")
   public void run() {
      logger = new Logger(releaseName);
      Driver driver = new Driver();
      Timer timer = new Timer();
      beforeCheck();
      timer.schedule(new CheckTask(driver, timer), 0);
   }
   
   @AllArgsConstructor
   class CheckTask extends TimerTask {
      Driver driver;
      Timer timer;
      
      @Override
      public void run() {
         long sec = 1000;
         long min = sec * 60;
         long hour = 60 * min;
         driver.check();
         timer.schedule(new CheckTask(driver, timer),
           current().nextInt(60, 180) * min);
      }
   }
   
   @SneakyThrows
   protected void login() {
      FirefoxOptions options = new FirefoxOptions().setLogLevel(Level.OFF);
      driver = new FirefoxDriver(options);
      logger.log("Login");
      driver.get(loginUrl);
      driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
      beforeLogin();
      // Enter Username
      WebElement nameField = driver.findElement(nameFieldNavigator);
      nameField.sendKeys(USERNAME);
      // Enter Password
      WebElement passwordField = driver.findElement(passFieldNavigator);
      passwordField.sendKeys(PASS);
      // Click Login
      driver.findElement(submitButtonNavigator).click();
      sleep(1_000);
      afterLogin();
   }
   
   @SneakyThrows
   public void fullScrape() {
      String firstDate = getFirstDate();
      boolean releaseIsOld = releaseIsOld(firstDate);
      // If release found -> scrape all links and date
      boolean newReleaseOnThePool = downloaded
        .find(eq("releaseDate", firstDate)).first() == null;
      boolean oldReleaseWasDownloaded = downloaded
        .find(eq("oldReleaseDate", firstDate)).first() != null;
      if (newReleaseOnThePool) {
         logger.log("Downloading New Release");
         setCookieForAPI();
         //  Find next date
         String downloadDate = getDownloadDate(firstDate);
         if (downloaded.find(eq("oldReleaseDate", downloadDate))
           .first() == null) {
            // MAIN OPERATION EXECUTION
            mainOperation(firstDate, downloadDate);
         }
         // Schedule release and add to DB
         downloaded.insertOne(
           new Document("releaseDate", firstDate));
      } else if (releaseIsOld && !oldReleaseWasDownloaded) {
         logger.log("Downloading Old Release");
         setCookieForAPI();
         mainOperation(firstDate, firstDate);
         downloaded.insertOne(
           new Document("oldReleaseDate", firstDate));
      }
   }
   
   @Override
   public void secondStageCheck() {
      fullScrape();
   }
   
   @Override
   public void firstStageCheck() {
      if (loginAtFirstStage) {
         login();
      } else {
         getDriverPage();
      }
      afterFirstStage();
   }
   
   @SneakyThrows
   private String getFirstDate() {
      while (true) {
         try {
            return scrapeFirstDate(getPageSource());
         } catch (NullPointerException e) {
            driver.get(urlToGet);
            Thread.sleep(10_000);
         }
      }
   }
   
   @SneakyThrows
   private void getDriverPage() {
      driver = new FirefoxDriver(firefoxOptions);
      driver.get(urlForFirstStage);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
   }
   
   protected String getDownloadDate(String firstDate) {
      while (true) {
         String html = getPageSource();
         String downloadDate = previousDateOnThisPage(html, firstDate);
         boolean dateOnThisPage = downloadDate != null;
         if (dateOnThisPage) {
            return downloadDate;
         } else {
            nextPage();
         }
      }
   }
   
   protected List<String> scrapeLinks(String firstDate, String downloadDate) {
      List<String> scrapedLinks = new ArrayList<>();
      while (true) {
         scrapeAllLinksOnPage(getPageSource(), downloadDate,
           firstDate, scrapedLinks);
         nextPage();
         String dateOnTopOfThePage = getFirstDate();
         boolean noDownloadDateOnThePage = !dateOnTopOfThePage.equals(firstDate)
           && !dateOnTopOfThePage.equals(downloadDate);
         if (noDownloadDateOnThePage) {
            operationWithLinksAfterScrape(scrapedLinks);
            return scrapedLinks;
         }
      }
   }
   
   protected void mainOperation(String firstDate, String downloadDate) {
      scrapeAndDownloadRelease(firstDate, downloadDate, releaseName);
   }
   
   protected void scrapeAndDownloadRelease(String firstDate, String downloadDate,
                                           String releaseName) {
      List<String> scrapedLinks = scrapeLinks(firstDate, downloadDate);
      if (scrapedLinks.size() > 0) {
         writeLinksToDB(scrapedLinks,
           releaseName + " " + formatDownloadDate(downloadDate));
         downloadLinks(scrapedLinks,
           releaseName + " " + formatDownloadDate(downloadDate));
      }
   }
   
   private void writeLinksToDB(List<String> scrapedLinks, String releaseName) {
      boolean noScrapedReleaseInDB = mongoControl.scrapedReleases
        .find(eq("releaseName", releaseName)).first() == null;
      if (noScrapedReleaseInDB) {
         mongoControl.scrapedReleases
           .insertOne(new Document("releaseName", releaseName)
             .append("scrapedLinks", scrapedLinks));
      }
   }
   
   @SneakyThrows
   private String formatDownloadDate(String date) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new SimpleDateFormat(dateFormat, Locale.US).parse(date));
      cal.add(Calendar.DAY_OF_MONTH, 1);
      return new SimpleDateFormat("ddMM").format(cal.getTime());
   }
   
   private boolean releaseIsOld(String date) throws ParseException {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new SimpleDateFormat(dateFormat, Locale.US).parse(date));
      cal.add(Calendar.DAY_OF_MONTH, 2);
      long nowTime = new Date().getTime();
      return nowTime > cal.getTime().getTime();
   }
   
   public String getPageSource() {
      String javascript = "return document.getElementsByTagName('html')[0].innerHTML";
      String pageSource = (String) ((JavascriptExecutor) driver).executeScript(javascript,
        driver.findElement(By.tagName("html")));
      return "<html>" + pageSource + "</html>";
   }
   
   protected void setCookieForAPI() {
      cookieForAPI = driver.manage().getCookies().stream()
        .map(cookie -> cookie.getName() + "=" + cookie.getValue())
        .collect(Collectors.joining("; "));
   }
   
   @Override
   public String getCookie() {
      return cookieForAPI;
   }
   
   @Override
   public Logger getLogger() {
      return logger;
   }
   
   private class Driver {
      private void check() {
         try {
            firstStageCheck();
            betweenStages();
            secondStageCheck();
         } catch (Exception e) {
            logger.log(e);
         } finally {
            if (exitAfterCheck) {
               driver.quit();
            }
         }
      }
   }
   
}
