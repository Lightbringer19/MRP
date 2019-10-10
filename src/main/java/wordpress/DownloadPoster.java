package wordpress;

import configuration.YamlConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.Constants;
import utils.Logger;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DownloadPoster {
   
   protected static WebDriver driver;
   
   protected Logger logger;
   
   protected String loginUrl = "https://myrecordpool.com/wp-login.php";
   protected By nameFieldNavigator = By.id("user_login");
   protected By passFieldNavigator = By.id("user_pass");
   protected By submitButtonNavigator = By.id("wp-submit");
   
   protected static YamlConfig.Config yamlConfig = new YamlConfig().config;
   
   protected String USERNAME;
   protected String PASS;
   
   public DownloadPoster() {
      System.setProperty("webdriver.gecko.driver", Constants.filesDir + "geckodriver.exe");
      USERNAME = yamlConfig.getMrp_user();
      PASS = yamlConfig.getMrp_password();
      login();
   }
   
   public static void main(String[] args) {
      DownloadPoster downloadPoster = new DownloadPoster();
      // downloadPoster.login();
      // downloadPoster.addDownload("ANOTHER TEST", "https://www.google.com.ua/");
   }
   
   String addDownload(String title, String uploadLink) {
      driver.get("https://myrecordpool.com/wp-admin/post-new.php?post_type=sdm_downloads");
      driver.findElement(By.id("title")).sendKeys(title);
      driver.findElement(By.id("sdm_upload")).sendKeys(uploadLink);
      driver.findElement(By.id("publish")).click();
      String html = driver.getPageSource();
      Document document = Jsoup.parse(html);
      String postID = document.select("input[id=post_ID]").attr("value");
      System.out.println("Download ID for " + title + " : " + postID);
      
      return postID;
   }
   
   protected void login() {
      FirefoxOptions options = new FirefoxOptions().setLogLevel(Level.OFF);
      driver = new FirefoxDriver(options);
      // logger.log("Login");
      driver.get(loginUrl);
      driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
      // Enter Username
      WebElement nameField = driver.findElement(nameFieldNavigator);
      nameField.sendKeys(USERNAME);
      // Enter Password
      WebElement passwordField = driver.findElement(passFieldNavigator);
      passwordField.sendKeys(PASS);
      // Click Login
      driver.findElement(submitButtonNavigator).click();
   }
}
