package scraper.maletadvj;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

import java.text.MessageFormat;
import java.util.List;

public class MaletaDvjScraper extends Scraper {
    
    public MaletaDvjScraper() {
        USERNAME = yamlConfig.getMaletadvj_username();
        PASS = yamlConfig.getMaletadvj_password();
    
        loginUrl = "https://maletadvj.com/";
        nameFieldNavigator = By.id("email");
        passFieldNavigator = By.id("password");
        submitButtonNavigator = By.id("login-btn");
    
        dateFormat = "dd/MM/yyyy";
        downloaded = mongoControl.maletadvjDownloaded;
        releaseName = "MyRecordPool Latin";
    }
    
    public static void main(String[] args) {
        MaletaDvjScraper maletaDvjScraper = new MaletaDvjScraper();
        maletaDvjScraper.start();
    }
    
    @Override
    @SneakyThrows
    public void beforeLogin() {
        driver.findElement(By.cssSelector(".mini-cart > a:nth-child(1)")).click();
        Thread.sleep(500);
    }
    
    @Override
    public void afterFirstStage() {
        driver.get("https://maletadvj.com/audios");
    }
    
    @Override
    protected void mainOperation(String firstDate, String downloadDate) {
        logger.log("Downloading Music Release");
        scrapeAndDownloadRelease(firstDate, downloadDate,
           releaseName + " Audio");
        // SCRAPE VIDEOS AND DOWNLOAD
        urlToGet = "https://maletadvj.com/videos";
        driver.get(urlToGet);
        logger.log("Looking for Video Release");
        scrapeAndDownloadRelease(firstDate, downloadDate,
           releaseName + " Videos");
    }
    
    @Override
    public String scrapeFirstDate(String html) {
        return Jsoup.parse(html).select("small:contains(Creado el)")
           .first().text()
           .replace("- Creado el: ", "");
    }
    
    @Override
    public String previousDateOnThisPage(String html, String firstDate) {
        return Jsoup.parse(html)
           .select("small:contains(Creado el)")
           .stream()
           .map(Element::text)
           .map(date -> date.replace("- Creado el: ", ""))
           .filter(date -> !date.equals(firstDate))
           .findFirst()
           .orElse(null);
    }
    
    @Override
    public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
        Elements trackInfos = Jsoup.parse(html).select("tr[class=table_product]");
        for (Element trackInfo : trackInfos) {
            String trackDate = trackInfo.select("small:contains(Creado el)")
               .first().text()
               .replace("- Creado el: ", "");
            if (trackDate.equals(downloadDate)) {
                String trackName = trackInfo.select("h3").first().text();
                String downloadID = trackInfo.attr("data-product_id");
                String downloadUrl = MessageFormat.format(
                   "https://maletadvj.com/products/descargar_producto/" +
                      "{0}", downloadID);
                scrapedLinks.add(downloadUrl);
                System.out.println(trackName + " | " + downloadUrl);
            }
        }
    }
    
    @Override
    public void nextPage() {
        String currentUrl = driver.getCurrentUrl();
        String pageNumber = currentUrl.substring(currentUrl.lastIndexOf("/") + 1);
        String newLink;
        if (!pageNumber.matches("^[0-9]+$")) {
            newLink = currentUrl + "/" + 20;
        } else {
            newLink = currentUrl.replace(pageNumber,
               String.valueOf(Integer.parseInt(pageNumber) + 20));
        }
        driver.get(newLink);
    }
}
