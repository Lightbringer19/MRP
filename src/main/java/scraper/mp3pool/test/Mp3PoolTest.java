package scraper.mp3pool.test;

import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

import java.util.List;
import java.util.stream.Collectors;

public class Mp3PoolTest extends Scraper {
    
    public static void main(String[] args) {
        Mp3PoolTest mp3PoolTest = new Mp3PoolTest();
        mp3PoolTest.run();
    }
    
    private Mp3PoolTest() {
        super("MyMp3Pool");
        System.setProperty("jsse.enableSNIExtension", "false");
        USERNAME = yamlConfig.getMp3_pool_username();
        PASS = yamlConfig.getMp3_pool_password();
        dateFormat = "MM/dd/yyyy";
        loginUrl = "https://mp3poolonline.com/user/login";
        nameFieldNavigator = By.id("edit-name");
        passFieldNavigator = By.id("edit-pass");
        submitButtonNavigator = By.id("edit-submit");
        downloaded = mongoControl.mp3PoolDownloaded;
        releaseName = "MyMp3Pool";
    }
    
    @Override
    protected void operationWithLinksAfterScrape(List<String> scrapedLinks) {
        driver.quit();
        List<String> duplicates = scrapedLinks.stream()
           .filter(scrapedLink -> scrapedLink.endsWith("/"))
           .collect(Collectors.toList());
        scrapedLinks.removeAll(duplicates);
    }
    
    @Override
    protected void nextPage() {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("page")) {
            int pageNumber = Integer.parseInt(currentUrl.substring(currentUrl.indexOf("=") + 1));
            driver.get("https://mp3poolonline.com/viewadminaudio?page=" + (pageNumber + 1));
        } else {
            driver.get("https://mp3poolonline.com/viewadminaudio?page=1");
        }
    }
    
    @Override
    public String scrapeDate(String html) {
        return Jsoup.parse(html)
           .select("div[class=innerPlayer1]").first()
           .select("p").first().text()
           .replace("Added On: ", "");
    }
    
    @Override
    public String previousDateOnThisPage(String html, String date) {
        return Jsoup.parse(html).select("div[class=innerPlayer1]")
           .stream()
           .map(release -> release.select("p").first().text()
              .replace("Added On: ", ""))
           .filter(releaseDate -> !releaseDate.equals(date))
           .findFirst()
           .orElse(null);
    }
    
    @Override
    public void scrapeAllLinksOnPage(String html, String date, List<String> scrapedLinks) {
        Jsoup.parse(html)
           .select("div[class=innerPlayer1]")
           .stream()
           .filter(release -> release.select("p").first().text()
              .replace("Added On: ", "").equals(date))
           .forEach(release -> release.select("div>ul>li")
              .forEach(track -> {
                  if (!track.select("div[class=track-title]").text().equals("")) {
                      logger.log("Adding Track to Download List: "
                         + track.select("div[class=track-title]").text());
                  }
                  track.select("div[class=download2 sub-section]>a")
                     .stream()
                     .map(link -> link.attr("href"))
                     .filter(downloadUrl -> downloadUrl.contains("download/"))
                     .forEach(scrapedLinks::add);
              }));
    }
}
