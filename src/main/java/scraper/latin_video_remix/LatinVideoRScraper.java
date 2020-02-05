package scraper.latin_video_remix;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import reactor.core.publisher.Flux;
import scraper.abstraction.Scraper;
import utils.FUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static utils.CheckDate.getCurrentYear;
import static utils.CheckDate.getTodayDate;
import static utils.Constants.tagsDir;

public class LatinVideoRScraper extends Scraper {
   
   public LatinVideoRScraper() {
      USERNAME = yamlConfig.getLatinvideoremix_username();
      PASS = yamlConfig.getLatinvideoremix_password();
      dateFormat = "yyyy-MM-dd";
      loginUrl = "http://www.latinvideoremix.com/preindex.php";
      nameFieldNavigator = By.id("login_username");
      passFieldNavigator = By.id("login_password");
      submitButtonNavigator = By.className("btn_submit");
      downloaded = mongoControl.latinVideoRemixDownloaded;
      releaseName = "LatinVideoRemix Videos";
   }
   
   public static void main(String[] args) {
      LatinVideoRScraper latinVideoRScraper = new LatinVideoRScraper();
      latinVideoRScraper.run();
   }
   
   @Override
   @SneakyThrows
   public void afterLogin() {
      driver.get("http://www.latinvideoremix.com/catalog");
      sleep(1000);
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html)
        .select("tr[class*=tr_box]>td")
        .first().text();
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("tr[class*=tr_box]")
        .stream()
        .map(trackInfo -> trackInfo.select("td").first().text())
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate,
                                    List<String> scrapedLinks) {
      scrapedLinks.addAll(Jsoup.parse(html)
        .select("tr[class*=tr_box]")
        .stream()
        .filter(element -> element.select("td").first().text().equals(downloadDate))
        .map(element -> "http://www.latinvideoremix.com/" +
          element.select("a").last().attr("href")
            .replace("Javascript:frame('", "")
            .replace("');", ""))
        .collect(Collectors.toList()));
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      List<WebElement> navButtons = driver
        .findElement(By.className("paginado"))
        .findElements(By.tagName("li"));
      navButtons.get(navButtons.indexOf(navButtons.stream()
        .filter(webElement -> webElement.getAttribute("class")
          .equals("activo rounded-corners"))
        .findFirst()
        .orElse(null)) + 1).click();
      sleep(2000);
   }
   
   @Override
   public void downloadLinks(List<String> scrapedLinks, String releaseName) {
      getLogger().log("Downloading release: " + releaseName);
      String releaseFolderPath =
        "E://TEMP FOR LATER/" + getCurrentYear() + "/" + getTodayDate() +
          "/RECORDPOOL/" + releaseName + "/";
      new File(releaseFolderPath).mkdirs();
      String downloadLink = "http://www.latinvideoremix.com/download_start.php?file=67049";
      Flux.fromIterable(scrapedLinks)
        .doOnNext(downloadUrl -> {
           apiCall(downloadUrl);
           downloadFile(downloadLink, releaseFolderPath);
        })
        .blockLast();
      getLogger().log("Release Downloaded: " + releaseName);
      FUtils.writeFile(tagsDir.replace("\\Scrapers", ""), releaseName + ".json",
        releaseFolderPath);
      getLogger().log("Release added to tag queue: " + releaseName);
   }
   
   private void apiCall(String downloadUrl) {
      try {
         @Cleanup CloseableHttpClient client = HttpClients.createDefault();
         HttpGet get = new HttpGet(downloadUrl);
         RequestConfig.Builder builder = RequestConfig.custom();
         RequestConfig requestConfig = builder.setRedirectsEnabled(false).build();
         get.setConfig(requestConfig);
         get.setHeader("Cookie", getCookie());
         get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
         @Cleanup CloseableHttpResponse response = client.execute(get);
         System.out.println(downloadUrl + " | " + response.getStatusLine().getStatusCode());
      } catch (Exception e) {
         getLogger().log(e);
      }
   }
}
