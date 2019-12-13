package scraper;

import configuration.YamlConfig;
import mongodb.MongoControl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import scraper.abstraction.DownloadInterface;
import scraper.bpm.BpmApiService;
import utils.Constants;
import utils.Logger;

import java.util.concurrent.TimeUnit;

public class DownloaderTest implements DownloadInterface, BpmApiService {
   protected static MongoControl mongoControl = new MongoControl();
   protected static YamlConfig.Config yamlConfig = new YamlConfig().config;
   protected static WebDriver driver;
   private static String cookieForAPI;
   protected Logger logger = new Logger("TEST");
   
   protected String USERNAME;
   protected String PASS;
   protected String loginUrl;
   
   protected By nameFieldNavigator;
   protected By passFieldNavigator;
   protected By submitButtonNavigator;
   
   public static void main(String[] args) {
      // String[] links = {
      // // };
      cookieForAPI = "";
      DownloaderTest downloaderTest = new DownloaderTest();
      // List<String> latino = downloaderTest.getDownloadInfo("", "latino");
      // System.out.println(latino);
      String url = "";
      String location = downloaderTest.getLocation(downloaderTest.getLocation(url));
      System.out.println(location);
      // String testLocation = downloaderTest.getLocation(location).replaceAll(" ", "%20");
      // System.out.println(testLocation);
      
      // String downloadURL = "";
      // downloaderTest.downloadLinks(Collections.singletonList(downloadURL), "TEST");
      
      // downloaderTest.USERNAME = yamlConfig.getBpm_username();
      // downloaderTest.PASS = yamlConfig.getBpm_password();
      // downloaderTest.loginUrl = "https://www.bpmsupreme.com/login";
      // downloaderTest.nameFieldNavigator = By.id("login-form-email");
      // downloaderTest.passFieldNavigator = By.id("login-form-password");
      // downloaderTest.submitButtonNavigator = By.tagName("button");
      //
      // try {
      //    downloaderTest.login();
      //    driver.get("https://www.bpmsupreme.com/store/newreleases/video/classic/1");
      //    Thread.sleep(10_000);
      //    cookieForAPI = driver.manage().getCookies().stream()
      //      .map(cookie -> cookie.getName() + "=" + cookie.getValue())
      //      .collect(Collectors.joining("; "));
      //
      //    // String releaseName = "Bpm Supreme 0808";
      //    // Document scrapedRelease = mongoControl.scrapedReleases
      //    //    .find(eq("releaseName", releaseName)).first();
      //    // List<String> scrapedLinks = (List<String>) scrapedRelease.get("scrapedLinks");
      //
      //    List<String> downloadInfo = downloaderTest.getDownloadInfo("https://www.bpmsupreme.com/store/output_file/314217");
      //    String downloadURL = downloadInfo.get(0);
      //    String cookie = downloadInfo.get(1);
      //    System.out.println(cookie);
      //    cookieForAPI = cookie;
      //
      //    downloaderTest.downloadLinks(Collections.singletonList(downloadURL), "TEST");
      //
      // } catch (Exception e) {
      //    e.printStackTrace();
      // } finally {
      //    driver.quit();
      // }
      
   }
   
   protected void login() {
      System.setProperty("webdriver.gecko.driver", Constants.filesDir + "geckodriver.exe");
      driver = new FirefoxDriver();
      logger.log("Login");
      driver.get(loginUrl);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      // Enter Username
      WebElement nameField = driver.findElement(nameFieldNavigator);
      nameField.sendKeys(USERNAME);
      // Enter Password
      WebElement passwordField = driver.findElement(passFieldNavigator);
      passwordField.sendKeys(PASS);
      // Click Login
      driver.findElement(submitButtonNavigator).click();
   }
   
   @Override
   public String getCookie() {
      return cookieForAPI;
   }
   
   @Override
   public Logger getLogger() {
      return logger;
   }
}
