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
import utils.Constants;
import utils.Logger;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

public abstract class Scraper extends Thread implements ScrapeInterface, DownloadInterface {
    
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
    
    public Scraper() {
        System.setProperty("webdriver.gecko.driver", Constants.filesDir + "geckodriver.exe");
    }
    
    @Override
    public void run() {
        logger = new Logger(releaseName);
        Timer timer = new Timer();
        Driver driver = new Driver();
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
                driver = new FirefoxDriver();
                login();
                afterLogin();
                String dateOnFirstPage = scrapeDate(driver.getPageSource());
                // If release found -> scrape all links and date
                boolean newReleaseOnThePool = downloaded
                   .find(eq("releaseDate", dateOnFirstPage)).first() == null;
                if (newReleaseOnThePool) {
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
                driver.quit();
            }
        }
        
        private void login() {
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
        
        private String getDownloadDate(String dateOnFirstPage) {
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
    }
    
    private List<String> scrapeLinks(String dateOnFirstPage, String dateToDownload) {
        List<String> scrapedLinks = new ArrayList<>();
        while (true) {
            scrapeAllLinksOnPage(driver.getPageSource(), dateToDownload, scrapedLinks);
            nextPage();
            String dateOnTopOfThePage = scrapeDate(driver.getPageSource());
            boolean noDownloadDateOnThePage = !dateOnTopOfThePage.equals(dateOnFirstPage)
               && !dateOnTopOfThePage.equals(dateToDownload);
            if (noDownloadDateOnThePage) {
                operationWithLinksAfterScrape(scrapedLinks);
                return scrapedLinks;
            }
        }
    }
    
    @SneakyThrows
    private String formatDateToDownload(String date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new SimpleDateFormat(dateFormat, Locale.US).parse(date));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat("ddMM").format(cal.getTime());
    }
    
    protected void mainOperation(String dateOnFirstPage, String dateToDownload) {
        scrapeAndDownloadRelease(dateOnFirstPage, dateToDownload, releaseName);
    }
    
    protected void scrapeAndDownloadRelease(String dateOnFirstPage, String dateToDownload,
                                            String releaseName) {
        //Scrape all links
        List<String> scrapedLinks = scrapeLinks(dateOnFirstPage, dateToDownload);
        //  Download
        if (scrapedLinks.size() > 0) {
            downloadLinks(scrapedLinks,
               releaseName + " " + formatDateToDownload(dateToDownload));
        }
    }
    
    @Override
    public String getCookie() {
        return cookieForAPI;
    }
    
    @Override
    public Logger getLogger() {
        return logger;
    }
    
    protected abstract void afterLogin();
    
    protected abstract void operationWithLinksAfterScrape(List<String> scrapedLinks);
    
    protected abstract void nextPage();
}
