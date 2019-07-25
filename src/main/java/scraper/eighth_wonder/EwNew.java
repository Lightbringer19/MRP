package scraper.eighth_wonder;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import scraper.abstraction.Scraper;

import java.util.List;

public class EwNew extends Scraper {
    
    private EwNew() {
        USERNAME = yamlConfig.getEw_username();
        PASS = yamlConfig.getEw_password();
        dateFormat = "MM.dd.yy";
        loginUrl = "https://www.8thwonderpromos.com/amember/login";
        nameFieldNavigator = By.id("amember-login");
        passFieldNavigator = By.id("amember-pass");
        submitButtonNavigator = By.className("password-link");
        downloaded = mongoControl.ewDownloaded;
        releaseName = "8th Wonder Pool";
    }
    
    public static void main(String[] args) {
        EwNew ewNew = new EwNew();
        ewNew.run();
    }
    
    @Override
    @SneakyThrows
    protected void afterLogin() {
        Thread.sleep(10_000);
        
    }
    
    @Override
    @SneakyThrows
    protected void mainOperation(String dateOnFirstPage, String dateToDownload) {
        logger.log("Downloading Music Release");
        scrapeAndDownloadRelease(dateOnFirstPage, dateToDownload, releaseName);
        // SCRAPE VIDEOS AND DOWNLOAD
        driver.findElement(By.linkText("Video")).click();
        Thread.sleep(10_000);
        logger.log("Looking for Video Release");
        scrapeAndDownloadRelease(dateOnFirstPage, dateToDownload,
           releaseName + " Videos");
    }
    
    @Override
    @SneakyThrows
    protected void nextPage() {
        List<WebElement> pagination_arw = driver.findElements(By.className("pagination_arw"));
        if (pagination_arw.size() > 2) {
            pagination_arw.get(1).click();
        } else {
            pagination_arw.get(0).click();
        }
        Thread.sleep(10_000);
    }
    
    @Override
    public String scrapeDate(String html) {
        Element firstTrack = Jsoup.parse(html).select("div[class=tracks-list__item]").first();
        Element trackInfo = firstTrack.select("div[class=col-sm-4 m-w-100]").first();
        return trackInfo.select("div[class=col-sm-12 m-mar-l-20p " +
           "no-padding]>div[class=col-sm-2 no-padding]").first().text();
    }
    
    @Override
    public String previousDateOnThisPage(String html, String date) {
        return Jsoup.parse(html)
           .select("div[class=tracks-list__item]").stream()
           .map(track -> track.select("div[class=col-sm-4 m-w-100]").first())
           .map(trackInfo -> trackInfo.select("div[class=col-sm-12 m-mar-l-20p " +
              "no-padding]>div[class=col-sm-2 no-padding]").first().text())
           .filter(releaseDate -> !releaseDate.equals(date))
           .findFirst()
           .orElse(null);
    }
    
    @Override
    public void scrapeAllLinksOnPage(String html, String date, List<String> scrapedLinks) {
        Jsoup.parse(html).select("div[class=tracks-list__item]").forEach(track -> {
            Element trackInfo = track.select("div[class=col-sm-4 m-w-100]").first();
            String releaseDate = trackInfo.select("div[class=col-sm-12 m-mar-l-20p " +
               "no-padding]>div[class=col-sm-2 no-padding]").first().text();
            if (releaseDate.equals(date)) {
                String downloadUrl = "https://pool.8thwonderpromos.com" +
                   track.select("a[class=btn-download tracks-list__" +
                      "action-button download-btn]").attr("href");
                logger.log("Adding Track to Download List: "
                   + trackInfo.select("h4[class=cursor-pointer]")
                   .first().attr("title") + " | " + downloadUrl);
                scrapedLinks.add(downloadUrl);
            }
        });
    }
    
    @Override
    protected void operationWithLinksAfterScrape(List<String> scrapedLinks) {
    }
    
}
