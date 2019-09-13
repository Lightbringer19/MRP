package scraper.heavyhits;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

import java.util.List;

public class HeavyHitsScraper extends Scraper implements HeavyHitsApiService {
   public HeavyHitsScraper() {
      USERNAME = yamlConfig.getHeavyhits_username();
      PASS = yamlConfig.getHeavyhits_password();
      dateFormat = "MM.dd.yyyy";
      loginUrl = "https://www.heavyhits.com/members/login";
      nameFieldNavigator = By.id("amember-login");
      passFieldNavigator = By.id("amember-pass");
      submitButtonNavigator = By.cssSelector("div.am-row:nth-child(5) >" +
        " div:nth-child(1) > input:nth-child(1)");
      downloaded = mongoControl.heavyHitsDownloaded;
      releaseName = "Heavy Hits";
   }
   
   public static void main(String[] args) {
      HeavyHitsScraper heavyHitsScraper = new HeavyHitsScraper();
      heavyHitsScraper.run();
   }
   
   @Override
   @SneakyThrows
   public void afterFirstStage() {
      urlToGet = "https://www.heavyhits.com/browse/#/new-releases?a";
      driver.get(urlToGet);
      Thread.sleep(10_000);
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html).select("time").first().text();
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("time")
        .stream()
        .map(Element::text)
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
      String nextDate = Jsoup.parse(html)
        .select("time")
        .stream()
        .map(Element::text)
        .filter(date -> !date.equals(firstDate) && !date.equals(downloadDate))
        .findFirst()
        .orElse("<nav class=\"navigation pagination\" role=\"navigation\">");
      String subHtml = html.substring(html.indexOf(downloadDate), html.indexOf(nextDate));
      Elements trackInfos = Jsoup.parse(subHtml).select("li[class*=row results]");
      for (Element trackInfo : trackInfos) {
         Elements downloadButtons = trackInfo.select("option[class*=player--version]");
         for (Element downloadButton : downloadButtons) {
            String downloadID = downloadButton.attr("data-id");
            String trackName = downloadButton.attr("data-f");
            String urlForApi = "https://www.heavyhits.com/hh18/library/download.php?id=" + downloadID;
            String downloadUrl = getDownloadUrl(urlForApi);
            scrapedLinks.add(downloadUrl);
            System.out.println(trackName + " | " + downloadUrl);
         }
      }
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      String currentUrl = driver.getCurrentUrl();
      if (!currentUrl.equals("https://www.heavyhits.com/browse/#/new-releases?a")) {
         String pageNumber = currentUrl.substring(currentUrl.lastIndexOf("/") + 1);
         urlToGet = "https://www.heavyhits.com/browse/#/new-releases/" +
           (Integer.parseInt(pageNumber) + 1);
         driver.get(urlToGet);
      } else {
         urlToGet = "https://www.heavyhits.com/browse/#/new-releases/2";
         driver.get(urlToGet);
      }
      Thread.sleep(10_000);
   }
}
