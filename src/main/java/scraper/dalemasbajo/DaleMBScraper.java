package scraper.dalemasbajo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import scraper.abstraction.Scraper;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DaleMBScraper extends Scraper implements LoginInterface {
    
    public DaleMBScraper() {
        USERNAME = yamlConfig.getDalemasbajo_username();
        PASS = yamlConfig.getDalemasbajo_password();
    
        dateFormat = "MM/dd/yy";
        downloaded = mongoControl.daleMasBajoDownloaded;
        releaseName = "DaleMasBajo";
    }
    
    @Override
    protected void login() {
        FirefoxOptions options = new FirefoxOptions().setLogLevel(Level.OFF);
        driver = new FirefoxDriver(options);
        logger.log("Login");
        driver.get("https://dalemasbajo.com/");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().deleteAllCookies();
        driver.manage().addCookie(getCookie(USERNAME, PASS));
        driver.get("https://dalemasbajo.com/");
    }
    
    @Override
    public String scrapeFirstDate(String html) {
        return Jsoup.parse(html).select("tr[id*=singleSongPlayer]>td").first().text();
    }
    
    @Override
    public String previousDateOnThisPage(String html, String firstDate) {
        return Jsoup.parse(html)
           .select("tr[id*=singleSongPlayer]")
           .stream()
           .map(trackInfo -> trackInfo.select("td").first().text())
           .filter(date -> !date.equals(firstDate))
           .findFirst()
           .orElse(null);
    }
    
    @Override
    public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
        Elements trackInfos = Jsoup.parse(html).select("tr[id*=singleSongPlayer]");
        for (Element trackInfo : trackInfos) {
            String trackDate = trackInfo.select("td").first().text();
            if (trackDate.equals(downloadDate)) {
                String trackName = trackInfo.text();
                String downloadID = trackInfo.attr("data-product");
                String downloadUrl = MessageFormat.format(
                   "https://dalemasbajo.com/products/descargar_producto/{0}",
                   downloadID);
                scrapedLinks.add(downloadUrl);
                System.out.println(trackName + " | " + downloadUrl);
            }
        }
    }
    
    @Override
    public void nextPage() {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.equals("https://dalemasbajo.com/")) {
            driver.get("https://dalemasbajo.com/20");
        } else {
            String pageNumber = currentUrl.substring(currentUrl.lastIndexOf("/") + 1);
            String newLink = "https://dalemasbajo.com/" +
               (Integer.parseInt(pageNumber) + 20);
            driver.get(newLink);
        }
    }
}
