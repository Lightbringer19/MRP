package scraper.crateconnect;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import scraper.abstraction.Scraper;

import java.text.MessageFormat;
import java.util.List;

public class CrateConnectScraper extends Scraper {
    
    public static void main(String[] args) {
        CrateConnectScraper crateConnectScraper = new CrateConnectScraper();
        crateConnectScraper.run();
    }
    
    public CrateConnectScraper() {
        USERNAME = yamlConfig.getCrateconnect_username();
        PASS = yamlConfig.getCrateconnect_password();
        loginUrl = "https://crateconnect.net/login";
        nameFieldNavigator = By.id("username");
        passFieldNavigator = By.id("password");
        submitButtonNavigator = By.cssSelector(".button");
    
        dateFormat = "MM/dd/yyyy";
        downloaded = mongoControl.crateConnectDownloaded;
        releaseName = "Crate Connect";
    }
    
    @Override
    @SneakyThrows
    public void afterLogin() {
        if (driver.getCurrentUrl().equals(loginUrl)) {
            // Enter Username
            WebElement nameField = driver.findElement(nameFieldNavigator);
            nameField.sendKeys(USERNAME);
            // Enter Password
            WebElement passwordField = driver.findElement(passFieldNavigator);
            passwordField.sendKeys(PASS);
            // Click Login
            driver.findElement(submitButtonNavigator).click();
            afterLogin();
        }
    }
    
    @Override
    @SneakyThrows
    public void afterFirstStage() {
        driver.get("https://crateconnect.net/record-pool");
        Thread.sleep(10_000);
        //sort by DATE
        String sortButtonXPath = "/html/body/div[1]/div/section/section[1]/div/form/div[1]" +
           "/div[3]/div/div[3]/div/div/div[1]/table/tbody/tr[1]/th[9]";
        driver.findElement(By.xpath(sortButtonXPath)).click();
        Thread.sleep(10_000);
        driver.findElement(By.xpath(sortButtonXPath)).click();
        Thread.sleep(10_000);
    }
    
    @Override
    public String scrapeFirstDate(String html) {
        return Jsoup.parse(html).select("td[class=created_at text-center]").first().text();
    }
    
    @Override
    public String previousDateOnThisPage(String html, String firstDate) {
        return Jsoup.parse(html)
           .select("td[class=created_at text-center]")
           .stream()
           .map(Element::text)
           .filter(date -> !date.equals(firstDate))
           .findFirst()
           .orElse(null);
    }
    
    @Override
    public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
        Elements trackInfos = Jsoup.parse(html).select("tr[class=cc-song song ]");
        for (Element trackInfo : trackInfos) {
            String trackDate = trackInfo.select("td[class=created_at text-center]").first().text();
            if (trackDate.equals(downloadDate)) {
                String trackName = trackInfo.text();
                String downloadID = trackInfo.attr("data-id");
                String downloadUrl = MessageFormat.format(
                   "https://crateconnect.net/index.php?option=com_crateconnect&format=raw&task=downloadZipFile&fileid={0}",
                   downloadID);
                scrapedLinks.add(downloadUrl);
                System.out.println(trackName + " | " + downloadUrl);
            }
        }
    }
    
    @Override
    @SneakyThrows
    public void nextPage() {
        Actions action = new Actions(driver);
        for (int i = 0; i < 20; i++) {
            action.sendKeys(Keys.PAGE_DOWN).build().perform();
        }
        Thread.sleep(500);
        driver.findElement(By.xpath("/html/body/div[1]/div/section/section[1]/div/form/div[1" +
           "]/div[3]/div/div[3]/div/div/div[2]/div/ul/li[13]")).click();
        Thread.sleep(10_000);
    }
}
