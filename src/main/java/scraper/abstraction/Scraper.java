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
import utils.FUtils;
import utils.Logger;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
   
   protected boolean checkOldReleases = true;
   protected boolean downloadFirstTimeScrapedPlaylist = true;
   
   public Scraper() {
      yamlConfig = new YamlConfig().config;
      mongoControl = new MongoControl();
      System.setProperty("webdriver.gecko.driver", Constants.filesDir + "geckodriver.exe");
      System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
      System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
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
      driver = new FirefoxDriver();
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
   
   @Override
   @SneakyThrows
   public void mainOperation() {
      String firstDate = getFirstDate();
      boolean releaseIsOld = false;
      boolean oldReleaseWasDownloaded = false;
      if (checkOldReleases) {
         releaseIsOld = releaseIsOld(firstDate);
         oldReleaseWasDownloaded = downloaded
           .find(eq("oldReleaseDate", firstDate)).first() != null;
      }
      // If release found -> scrape all links and date
      boolean newReleaseOnThePool = downloaded
        .find(eq("releaseDate", firstDate)).first() == null;
      if (newReleaseOnThePool) {
         logger.log("Downloading New Release");
         setCookieForAPI();
         //  Find next date
         String downloadDate = getDownloadDate(firstDate);
         if (downloaded.find(eq("oldReleaseDate", downloadDate))
           .first() == null) {
            scrapeAndDownloadOperation(firstDate, downloadDate);
         }
         // add to DB
         downloaded.insertOne(
           new Document("releaseDate", firstDate));
      } else if (releaseIsOld && !oldReleaseWasDownloaded) {
         logger.log("Downloading Old Release");
         setCookieForAPI();
         scrapeAndDownloadOperation(firstDate, firstDate);
         downloaded.insertOne(
           new Document("oldReleaseDate", firstDate));
      }
   }
   
   @Override
   public void driverCreationStage() {
      if (loginAtFirstStage) {
         login();
      } else {
         getProfileDriver();
      }
      afterDriverCreation();
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
   private void getProfileDriver() {
      driver = new FirefoxDriver(firefoxOptions);
      driver.get(urlForFirstStage);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
   }
   
   protected String getDownloadDate(String firstDate) {
      while (true) {
         String html = getPageSource();
         String downloadDate = previousDateOnThisPage(html, firstDate);
         if (downloadDate != null) {
            return downloadDate;
         } else {
            nextPage();
         }
      }
   }
   
   protected void scrapeAndDownloadOperation(String firstDate, String downloadDate) {
      scrapeAndDownloadRelease(firstDate, downloadDate, releaseName);
   }
   
   protected void scrapeAndDownloadRelease(String firstDate, String downloadDate,
                                           String releaseName) {
      List<String> scrapedLinks = scrapeLinks(firstDate, downloadDate);
      if (scrapedLinks.size() > 0) {
         String formattedDate = formatDownloadDate(downloadDate, dateFormat);
         writeLinksToDB(scrapedLinks, releaseName + " " + formattedDate);
         downloadLinks(scrapedLinks, releaseName + " " + formattedDate);
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
   
   protected void writeLinksToDB(List<String> scrapedLinks, String releaseName) {
      boolean noScrapedReleaseInDB = mongoControl.scrapedReleases
        .find(eq("releaseName", releaseName)).first() == null;
      if (noScrapedReleaseInDB) {
         mongoControl.scrapedReleases
           .insertOne(new Document("releaseName", releaseName)
             .append("scrapedLinks", scrapedLinks));
      }
   }
   
   protected void scrapeAndDownloadPlaylist(String playlistName, String playListUrl) {
      setCookieForAPI();
      logger.log("Scraping: " + playlistName);
      List<String> scrapedLinks = scrapePlaylist(playListUrl);
      Document playlistInDb = downloaded.find(eq("playlistName", playlistName)).first();
      if (playlistInDb != null) {
         List<String> scrapedLinksInDb = (List<String>) playlistInDb.get("scrapedLinks");
         int changedPercent = getChangedPercent(scrapedLinks, scrapedLinksInDb);
         if (changedPercent > 70) {
            downloadPlaylist(playlistName, scrapedLinks);
            playlistInDb.put("scrapedLinks", scrapedLinks);
            downloaded.findOneAndReplace(eq("playlistName", playlistName), playlistInDb);
         }
      } else {
         if (downloadFirstTimeScrapedPlaylist) {
            downloadPlaylist(playlistName, scrapedLinks);
         }
         downloaded.insertOne(new Document()
           .append("playlistName", playlistName)
           .append("scrapedLinks", scrapedLinks));
      }
   }
   
   protected void downloadPlaylist(String playlistName, List<String> scrapedLinks) {
      String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
      String playlistReleaseName =
        releaseName + " " + playlistName + " Playlist " + date;
      writeLinksToDB(scrapedLinks, playlistReleaseName);
      setCookieForAPI();
      downloadLinks(scrapedLinks, playlistReleaseName);
   }
   
   protected int getChangedPercent(List<String> scrapedLinks, List<String> oldScrape) {
      List<String> scrapedLinksTemp = new ArrayList<>(scrapedLinks);
      List<String> oldScrapeTemp = new ArrayList<>(oldScrape);
      int originalSize = scrapedLinksTemp.size();
      scrapedLinksTemp.removeAll(oldScrapeTemp);
      int changedPercent = (int) (scrapedLinksTemp.size() / ((float) originalSize / 100));
      logger.log(changedPercent + "% Changed");
      return changedPercent;
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
   
   @SneakyThrows
   protected void testNextPage() {
      for (int i = 0; i < 10; i++) {
         nextPage();
      }
      sleep(666666666);
   }
   
   protected void scrapingTesting() {
      String html = FUtils.readFile(new File("Files/source.html"));
      
      String firstDate = scrapeFirstDate(html);
      System.out.println("First date: " + firstDate);
      String downloadDate = previousDateOnThisPage(html, firstDate);
      System.out.println("Download date: " + firstDate);
      
      List<String> scrapedLinks = new ArrayList<>();
      scrapeAllLinksOnPage(html, downloadDate, firstDate, scrapedLinks);
      System.out.println("Scraped Links:");
      scrapedLinks.forEach(System.out::println);
   }
   
   @Override
   public String getCookie() {
      return cookieForAPI;
   }
   
   @Override
   public Logger getLogger() {
      return logger;
   }
   
   public void setDriver(WebDriver driver) {
      Scraper.driver = driver;
   }
   
   private class Driver {
      private void check() {
         try {
            driverCreationStage();
            betweenStages();
            scrapingStage();
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
