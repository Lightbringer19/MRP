package scraper.eighth_wonder;

import configuration.YamlConfig;
import mongodb.MongoControl;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.Constants;
import utils.Logger;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static scraper.eighth_wonder.EwTrackDownloader.downloadLinks;

class EwDriver {
    private static String USERNAME;
    private static String PASS;
    private WebDriver driver;
    static String cookieForAPI;
    static MongoControl mongoControl = new MongoControl();
    private EwScraper ewScraper = new EwScraper();
    static Logger ewLogger = new Logger("8thWonder");

    EwDriver() {
        String pathToSelenium = Constants.filesDir + "geckodriver.exe";
        System.setProperty("webdriver.gecko.driver", pathToSelenium);
        YamlConfig yamlConfig = new YamlConfig();
        USERNAME = yamlConfig.config.getEw_username();
        PASS = yamlConfig.config.getEw_password();
    }

    // public static void main(String[] args) throws ParseException, InterruptedException {
    //     EwDriver ewDriver = new EwDriver();
    //     ewDriver.ewCheck();
    // }

    void ewCheck() {
        while (true) {
            try {
                driver = new FirefoxDriver(new FirefoxOptions().setHeadless(true));
                Login();
                driver.get("https://pool.8thwonderpromos.com/");
                // SCRAPE PAGES TO DB
                Thread.sleep(10000);
                String html = driver.getPageSource();
                String dateOnFirstPage = ewScraper.scrapeDate(html);
                boolean newReleaseOnEW = mongoControl.ewDownloaded
                        .find(eq("releaseDate", dateOnFirstPage)).first() == null;
                // // if newest scraped date not found in DB
                if (newReleaseOnEW) {
                    //  GET COOKIES
                    cookieForAPI = driver.manage().getCookies().stream()
                            .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                            .collect(Collectors.joining("; "));
                    ewLogger.log("New Release Spotted");
                    // -> iterate over pages util the next date
                    String dateToDownload = getDownloadDate(html, dateOnFirstPage);
                    ewLogger.log("Downloading Music Release");
                    scrapeAndDownloadRelease(dateOnFirstPage, dateToDownload, "");
                    // SCRAPE VIDEOS AND DOWNLOAD
                    driver.findElement(By.linkText("Video")).click();
                    ewLogger.log("Looking for Video Release");
                    scrapeAndDownloadRelease(dateOnFirstPage, dateToDownload, "Videos ");
                    // INSERT DATE TO DB
                    mongoControl.ewDownloaded.insertOne(
                            new Document("releaseDate", dateOnFirstPage));
                }
                break;
            } catch (InterruptedException | ParseException | NullPointerException e) {
                ewLogger.log(e);
            } finally {
                driver.quit();
            }

        }
    }

    private void scrapeAndDownloadRelease(String dateOnFirstPage, String dateToDownload,
                                          String category)
            throws InterruptedException, ParseException {
        Thread.sleep(5000);
        // scrape all tracks from previous date
        List<String> scrapedLinks = scrapeLinks(dateOnFirstPage, dateToDownload);
        //  DOWNLOAD ALL TRACKS FROM DB VIA API REQUEST
        if (scrapedLinks.size() > 0) {
            downloadLinks(scrapedLinks, dateToDownload, category);
        }
    }

    private void Login() {
        ewLogger.log("Login");
        driver.get("https://www.8thwonderpromos.com/amember/login");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        // Enter Username
        WebElement nameField = driver.findElement(By.id("amember-login"));
        nameField.sendKeys(USERNAME);
        // Enter Password
        WebElement passwordField = driver.findElement(By.id("amember-pass"));
        passwordField.sendKeys(PASS);
        // Click Login
        driver.findElement(By.className("password-link")).click();
    }

    private List<String> scrapeLinks(String dateOnFirstPage, String dateToDownload) throws InterruptedException {
        List<String> scrapedLinks = new ArrayList<>();
        while (true) {
            String html = driver.getPageSource();
            scrapedLinks = ewScraper.scrapeAllLinksOnPage(html, dateToDownload, scrapedLinks);
            nextPage();
            //check date
            String dateOnTopOfThePage = ewScraper.scrapeDate(html);
            boolean noDownloadDateOnThePage = !dateOnTopOfThePage.equals(dateOnFirstPage)
                    && !dateOnTopOfThePage.equals(dateToDownload);
            if (noDownloadDateOnThePage) {
                return scrapedLinks;
            }
        }
    }

    @NotNull
    private String getDownloadDate(String html, String dateOnFirstPage)
            throws InterruptedException {
        while (true) {
            String downloadDate = ewScraper.previousDateOnThisPage(html, dateOnFirstPage);
            boolean dateOnThisPage = downloadDate != null;
            if (dateOnThisPage) {
                return downloadDate;
            } else {
                nextPage();
                html = driver.getPageSource();
            }
        }
    }

    private void nextPage() throws InterruptedException {
        List<WebElement> pagination_arw = driver.findElements(By.className("pagination_arw"));
        if (pagination_arw.size() > 2) {
            pagination_arw.get(1).click();
        } else {
            pagination_arw.get(0).click();
        }
        Thread.sleep(4000);
    }

    void quitDriver() {
        driver.quit();
    }

}
