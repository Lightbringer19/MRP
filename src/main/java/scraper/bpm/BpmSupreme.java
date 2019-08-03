package scraper.bpm;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.firefox.FirefoxDriver;
import scraper.abstraction.Scraper;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BpmSupreme extends Scraper implements ApiService {
    
    public static void main(String[] args) {
        BpmSupreme bpmSupreme = new BpmSupreme();
        bpmSupreme.run();
    }
    
    public BpmSupreme() {
        // USERNAME = yamlConfig.getEw_username();
        // PASS = yamlConfig.getEw_password();
        dateFormat = "MM.dd.yy";
        // loginUrl = "";
        // nameFieldNavigator = By.id("amember-login");
        // passFieldNavigator = By.id("amember-pass");
        // submitButtonNavigator = By.className("password-link");
        downloaded = mongoControl.bpmDownloaded;
        releaseName = "Bpm Supreme";
    }
    
    @Override
    @SneakyThrows
    public void firstStageForCheck() {
        driver = new FirefoxDriver(firefoxOptions);
        driver.get("https://www.bpmsupreme.com/store/newreleases/audio/classic/1");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        Thread.sleep(10_000);
    }
    
    @Override
    public String scrapeFirstDate(String html) {
        return Jsoup.parse(html).select("span[class=date ng-binding]").first().text();
    }
    
    @Override
    public String previousDateOnThisPage(String html, String firstDate) {
        return Jsoup.parse(html)
           .select("span[class=date ng-binding]")
           .stream()
           .filter(date -> !date.text().equals(firstDate))
           .findFirst()
           .map(Element::text)
           .orElse(firstDate);
    }
    
    @Override
    public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate,
                                     List<String> scrapedLinks) {
        
    }
    
    @Override
    @SneakyThrows
    public void nextPage() {
        driver.findElement(By.linkText("›")).click();
        Thread.sleep(10_000);
    }
}
