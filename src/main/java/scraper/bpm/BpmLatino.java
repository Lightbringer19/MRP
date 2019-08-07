package scraper.bpm;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import scraper.abstraction.Scraper;

import java.util.List;

public class BpmLatino extends Scraper {
    
    public static void main(String[] args) {
        BpmLatino bpmLatino = new BpmLatino();
        bpmLatino.run();
    }
    
    public BpmLatino() {
        USERNAME = yamlConfig.getBpm_latino_username();
        PASS = yamlConfig.getBpm_latino_password();
        loginUrl = "https://bpmlatino.com/store/index.php?option=com_user&view=login&Itemid=91";
        nameFieldNavigator = By.id("username");
        passFieldNavigator = By.id("passwd");
        submitButtonNavigator = By.className("login-btn1");
    
        dateFormat = "MM/dd/yy";
        downloaded = mongoControl.bpmLatinoDownloaded;
        releaseName = "Bpm Supreme Latino";
    }
    
    @Override
    @SneakyThrows
    public void afterFirstStage() {
        // urlToGet = "https://www.bpmsupreme.com/store/newreleases/audio/classic/1";
        // driver.get(urlToGet);
        Thread.sleep(10_000);
    }
    
    @Override
    @SneakyThrows
    protected void mainOperation(String firstDate, String downloadDate) {
        logger.log("Downloading Music Release");
        scrapeAndDownloadRelease(firstDate, downloadDate, releaseName);
        // SCRAPE VIDEOS AND DOWNLOAD
        urlToGet = "https://bpmlatino.com/store/index" +
           ".php?option=com_maianmedia&view=music&Itemid=2&cat_alias=videos&display=List";
        driver.get(urlToGet);
        Thread.sleep(10_000);
        logger.log("Looking for Video Release");
        scrapeAndDownloadRelease(firstDate, downloadDate,
           releaseName + " Videos");
    }
    
    @Override
    public String scrapeFirstDate(String html) {
        return Jsoup.parse(html).select("div[class=date_box]").first().text();
    }
    
    @Override
    public String previousDateOnThisPage(String html, String firstDate) {
        return Jsoup.parse(html)
           .select("div[class=date_box]")
           .stream()
           .filter(date -> !date.text().equals(firstDate))
           .findFirst()
           .map(Element::text)
           .orElse(null);
    }
    
    @Override
    public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
        Jsoup.parse(html).select("ul[class=songlist]>li").stream()
           .filter(trackInfo -> trackInfo.select("div[class=date_box]").text()
              .equals(downloadDate))
           .flatMap(trackInfo -> trackInfo
              .select("div[class=view_drop_block]>ul>li").stream())
           .forEach(downloadInfo -> {
               String downloadUrl = downloadInfo
                  .select("span[class=download_icon sprite ]>a")
                  .attr("href");
               scrapedLinks.add(downloadUrl);
               System.out.println(downloadInfo.select("p").text() + " | " + downloadUrl);
           });
    }
    
    @Override
    @SneakyThrows
    public void nextPage() {
        List<WebElement> nextButtons = driver.findElements(By.className("next"));
        for (WebElement nextButton : nextButtons) {
            if (nextButton.getAttribute("title").equals("Next")) {
                nextButton.click();
                break;
            }
        }
        Thread.sleep(10_000);
    }
}
