package scraper.mp3pool.old;

import configuration.YamlConfig;
import mongodb.MongoControl;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import utils.CheckDate;
import utils.Constants;
import utils.Logger;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static ftp.ScheduleWatcher.addToScheduleDB;
import static scraper.mp3pool.old.Mp3PoolDownloader.downloadLinks;

public class Mp3poolDriver {
    
    private static String USERNAME;
    private static String PASS;
    private WebDriver scrapeDriver;
    private static MongoControl mongoControl = new MongoControl();
    private Mp3PoolScraper mp3PoolScraper = new Mp3PoolScraper();
    static Logger mp3Logger = new Logger("Mp3Pool");
    static String cookieForAPI;
    
    Mp3poolDriver() {
        System.setProperty("jsse.enableSNIExtension", "false");
        
        String pathToSelenium = Constants.filesDir + "geckodriver.exe";
        System.setProperty("webdriver.gecko.driver", pathToSelenium);
        YamlConfig yamlConfig = new YamlConfig();
        
        USERNAME = yamlConfig.config.getMp3_pool_username();
        PASS = yamlConfig.config.getMp3_pool_password();
    }
    
    public static void main(String[] args) {
        Mp3poolDriver mp3poolDriver = new Mp3poolDriver();
        mp3poolDriver.check();
    }
    
    private void login(WebDriver driver) {
        mp3Logger.log("Login");
        driver.get("https://mp3poolonline.com/user/login");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        // Enter Username
        WebElement nameField = driver.findElement(By.id("edit-name"));
        nameField.sendKeys(USERNAME);
        // Enter Password
        WebElement passwordField = driver.findElement(By.id("edit-pass"));
        passwordField.sendKeys(PASS);
        // Click Login
        driver.findElement(By.id("edit-submit")).click();
    }
    
    void check() {
        scrapeDriver = new FirefoxDriver();
        try {
            login(scrapeDriver);
            String html = scrapeDriver.getPageSource();
            String dateOnFirstPage = mp3PoolScraper.scrapeDate(html);
            // If release found -> scrape all links and date
            boolean newReleaseOnMp3Pool = mongoControl.mp3PoolDownloaded
               .find(eq("releaseDate", dateOnFirstPage)).first() == null;
            if (newReleaseOnMp3Pool) {
                cookieForAPI = scrapeDriver.manage().getCookies().stream()
                   .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                   .collect(Collectors.joining("; "));
                //  Find next date
                String dateToDownload = getDownloadDate(html, dateOnFirstPage);
                System.out.println(dateToDownload);
                //Scrape all links
                List<String> scrapedLinks = scrapeLinks(dateOnFirstPage, dateToDownload);
                scrapeDriver.quit();
                //  Download
                String releaseFolderPath = getReleaseFolderPath(dateToDownload);
                if (scrapedLinks.size() > 0) {
                    downloadLinks(scrapedLinks, dateToDownload, releaseFolderPath);
                }
                // Schedule release and add to DB
                mp3Logger.log("Release Downloaded: " + dateToDownload);
                addToScheduleDB(new File(releaseFolderPath));
                mp3Logger.log("Release Scheduled: " + dateToDownload);
                mongoControl.mp3PoolDownloaded.insertOne(
                   new Document("releaseDate", dateOnFirstPage));
            }
        } catch (Exception e) {
            mp3Logger.log(e);
        } finally {
            scrapeDriver.quit();
        }
    }
    
    private List<String> scrapeLinks(String dateOnFirstPage, String dateToDownload) {
        List<String> scrapedLinks = new ArrayList<>();
        while (true) {
            String html = scrapeDriver.getPageSource();
            scrapedLinks = mp3PoolScraper.scrapeAllLinksOnPage(html, dateToDownload, scrapedLinks);
            nextPage();
            String dateOnTopOfThePage = mp3PoolScraper.scrapeDate(html);
            boolean noDownloadDateOnThePage = !dateOnTopOfThePage.equals(dateOnFirstPage)
               && !dateOnTopOfThePage.equals(dateToDownload);
            if (noDownloadDateOnThePage) {
                List<String> duplicates = scrapedLinks.stream()
                   .filter(scrapedLink -> scrapedLink.endsWith("/"))
                   .collect(Collectors.toList());
                scrapedLinks.removeAll(duplicates);
                return scrapedLinks;
            }
        }
    }
    
    private String getDownloadDate(String html, String dateOnFirstPage) {
        while (true) {
            String downloadDate = mp3PoolScraper.previousDateOnThisPage(html, dateOnFirstPage);
            boolean dateOnThisPage = downloadDate != null;
            if (dateOnThisPage) {
                return downloadDate;
            } else {
                nextPage();
                html = scrapeDriver.getPageSource();
            }
        }
    }
    
    private void nextPage() {
        String currentUrl = scrapeDriver.getCurrentUrl();
        if (currentUrl.contains("page")) {
            int pageNumber = Integer.parseInt(currentUrl.substring(currentUrl.indexOf("=") + 1));
            scrapeDriver.get("https://mp3poolonline.com/viewadminaudio?page=" + (pageNumber + 1));
        } else {
            scrapeDriver.get("https://mp3poolonline.com/viewadminaudio?page=1");
        }
    }
    
    private String getReleaseFolderPath(String date) throws ParseException {
        SimpleDateFormat DATE_FORMAT =
           new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.setTime(DATE_FORMAT.parse(date));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        String dateToDownload = new SimpleDateFormat("ddMM").format(cal.getTime());
        String releaseFolderPath =
           "Z:\\\\TEMP FOR LATER\\2019\\" + CheckDate.getTodayDate() +
              "\\RECORDPOOL\\" + ("MyMp3Pool " + dateToDownload) + "\\";
        new File(releaseFolderPath).mkdirs();
        return releaseFolderPath;
    }
}
