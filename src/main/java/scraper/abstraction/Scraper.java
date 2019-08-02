package scraper.abstraction;

import com.mongodb.client.MongoCollection;
import configuration.YamlConfig;
import lombok.SneakyThrows;
import mongodb.MongoControl;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import utils.Constants;
import utils.Logger;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

public abstract class Scraper extends Thread
   implements ScrapingInterface, DownloadInterface, ScraperInterface {
    
    protected static MongoControl mongoControl = new MongoControl();
    private static String cookieForAPI;
    protected static YamlConfig.Config yamlConfig = new YamlConfig().config;
    protected static WebDriver driver;
    
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
    
    protected boolean exitAfterCheck = true;
    protected final FirefoxOptions firefoxOptions;
    
    public Scraper() {
        System.setProperty("webdriver.gecko.driver", Constants.filesDir + "geckodriver.exe");
        FirefoxProfile ini = new ProfilesIni().getProfile("selenium");
        firefoxOptions = new FirefoxOptions().setProfile(ini);
    }
    
    @Override
    @SuppressWarnings("Duplicates")
    public void run() {
        logger = new Logger(releaseName);
        Timer timer = new Timer();
        Driver driver = new Driver();
        beforeCheck();
        TimerTask check = new TimerTask() {
            @Override
            @SneakyThrows
            public void run() {
                driver.check();
            }
        };
        long sec = 1000;
        long min = sec * 60;
        long hour = 60 * min;
        timer.schedule(check, 0, 6 * hour);
    }
    
    private class Driver {
        private void check() {
            try {
                firstStageForCheck();
                String dateOnFirstPage = scrapeDateOnFirstPage(driver.getPageSource());
                // If release found -> scrape all links and date
                boolean newReleaseOnThePool = downloaded
                   .find(eq("releaseDate", dateOnFirstPage)).first() == null;
                if (newReleaseOnThePool) {
                    // TODO: 30.07.2019
                    // if (true) {
                    // Get Cookies
                    cookieForAPI = driver.manage().getCookies().stream()
                       .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                       .collect(Collectors.joining("; "));
                    //  Find next date
                    String dateToDownload = getDownloadDate(dateOnFirstPage);
                    // MAIN OPERATION EXECUTION
                    mainOperation(dateOnFirstPage, dateToDownload);
                    // Schedule release and add to DB
                    downloaded.insertOne(
                       new Document("releaseDate", dateOnFirstPage));
                }
            } catch (Exception e) {
                logger.log(e);
            } finally {
                if (exitAfterCheck) {
                    driver.quit();
                }
            }
        }
    }
    
    protected void login() {
        driver = new FirefoxDriver(firefoxOptions);
        logger.log("Login");
        driver.get(loginUrl);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        // Enter Username
        WebElement nameField = driver.findElement(nameFieldNavigator);
        nameField.sendKeys(USERNAME);
        // Enter Password
        WebElement passwordField = driver.findElement(passFieldNavigator);
        passwordField.sendKeys(PASS);
        beforeLogin();
        // Click Login
        driver.findElement(submitButtonNavigator).click();
        afterLogin();
    }
    
    @Override
    public void firstStageForCheck() {
        login();
    }
    
    protected String getDownloadDate(String dateOnFirstPage) {
        while (true) {
            String html = driver.getPageSource();
            String downloadDate = previousDateOnThisPage(html, dateOnFirstPage);
            boolean dateOnThisPage = downloadDate != null;
            if (dateOnThisPage) {
                return downloadDate;
            } else {
                nextPage();
            }
        }
    }
    
    protected List<String> scrapeLinks(String dateOnFirstPage, String dateToDownload) {
        List<String> scrapedLinks = new ArrayList<>();
        while (true) {
            scrapeAllLinksOnPage(driver.getPageSource(), dateToDownload,
               dateOnFirstPage, scrapedLinks);
            nextPage();
            String dateOnTopOfThePage = scrapeDateOnFirstPage(driver.getPageSource());
            boolean noDownloadDateOnThePage = !dateOnTopOfThePage.equals(dateOnFirstPage)
               && !dateOnTopOfThePage.equals(dateToDownload);
            if (noDownloadDateOnThePage) {
                operationWithLinksAfterScrape(scrapedLinks);
                return scrapedLinks;
            }
        }
    }
    
    protected void mainOperation(String dateOnFirstPage, String dateToDownload) {
        scrapeAndDownloadRelease(dateOnFirstPage, dateToDownload, releaseName);
    }
    
    protected void scrapeAndDownloadRelease(String dateOnFirstPage, String dateToDownload,
                                            String releaseName) {
        List<String> scrapedLinks = scrapeLinks(dateOnFirstPage, dateToDownload);
        if (scrapedLinks.size() > 0) {
            downloadLinks(scrapedLinks,
               releaseName + " " + formatDateToDownload(dateToDownload));
        }
    }
    
    @SneakyThrows
    private String formatDateToDownload(String date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new SimpleDateFormat(dateFormat, Locale.US).parse(date));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat("ddMM").format(cal.getTime());
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
