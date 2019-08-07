package scraper.abstraction;

import com.mongodb.client.MongoCollection;
import configuration.YamlConfig;
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
                String firstDate = getFirstDate();
                // If release found -> scrape all links and date
                boolean newReleaseOnThePool = downloaded
                   .find(eq("releaseDate", firstDate)).first() == null;
                if (newReleaseOnThePool) {
                    // TODO: 02.08.2019
                    // if (true) {
                    // Get Cookies
                    cookieForAPI = driver.manage().getCookies().stream()
                       .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                       .collect(Collectors.joining("; "));
                    //  Find next date
                    String downloadDate = getDownloadDate(firstDate);
                    // MAIN OPERATION EXECUTION
                    mainOperation(firstDate, downloadDate);
                    // Schedule release and add to DB
                    downloaded.insertOne(
                       new Document("releaseDate", firstDate));
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
        beforeLogin();
        // Click Login
        driver.findElement(submitButtonNavigator).click();
        afterLogin();
    }
    
    @Override
    public void firstStageForCheck() {
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
            downloadLinks(scrapedLinks,
               releaseName + " " + formatDownloadDate(downloadDate));
        }
    }
    
    @SneakyThrows
    private String formatDownloadDate(String date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new SimpleDateFormat(dateFormat, Locale.US).parse(date));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat("ddMM").format(cal.getTime());
    }
    
    private String getPageSource() {
        String javascript = "return document.getElementsByTagName('html')[0].innerHTML";
        String pageSource = (String) ((JavascriptExecutor) driver).executeScript(javascript,
           driver.findElement(By.tagName("html")));
        return "<html>" + pageSource + "</html>";
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
