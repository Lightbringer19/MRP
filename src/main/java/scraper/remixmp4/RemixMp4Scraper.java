package scraper.remixmp4;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;

import java.text.MessageFormat;
import java.util.List;

public class RemixMp4Scraper extends Scraper {
   
   public RemixMp4Scraper() {
      USERNAME = yamlConfig.getRemixmp4_username();
      PASS = yamlConfig.getRemixmp4_password();
      dateFormat = "MM-dd-yyyy";
      loginUrl = "https://www.remixmp4.com/ingresar.php";
      nameFieldNavigator = By.name("user");
      passFieldNavigator = By.name("pass");
      submitButtonNavigator = By.cssSelector(".section > form:nth-child(2) > input:nth-child(3)");
      downloaded = mongoControl.remixMp4Downloaded;
      releaseName = "RemixMP4";
   }
   
   public static void main(String[] args) {
      RemixMp4Scraper remixMp4Scraper = new RemixMp4Scraper();
      remixMp4Scraper.run();
   }
   
   private static String getDate(String text) {
      int beginning = text.indexOf(" ", text.indexOf("Date: ")) + 1;
      int end = text.indexOf(" ", beginning);
      return text.substring(beginning, end);
   }
   
   @Override
   @SneakyThrows
   public void afterLogin() {
      Thread.sleep(2_000);
      driver.get("https://www.remixmp4.com/index.php?action=&name=&artist=&price1=&price2=&order=&genre=&page=1");
      Thread.sleep(2_000);
      
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return getDate(Jsoup.parse(html)
        .select("article[class=box_horizontal]").first().text());
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("article[class=box_horizontal]")
        .stream()
        .map(element -> getDate(element.text()))
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
      Elements trackInfos = Jsoup.parse(html).select("article[class=box_horizontal]");
      for (Element trackInfo : trackInfos) {
         String trackDate = getDate(trackInfo.text());
         if (trackDate.equals(downloadDate)) {
            String trackName = trackInfo.select("h3").first().text();
            Element downloadInfo = trackInfo.select("a[class=link_add]").first();
            String[] typeAndID = downloadInfo.attr("onclick")
              .replace("preview(", "")
              .replace(")", "").split(",");
            String downloadUrl = MessageFormat.format(
              "https://www.remixmp4.com/down.php?id={0}&type_={1}",
              typeAndID[1], typeAndID[0]);
            scrapedLinks.add(downloadUrl);
            System.out.println(trackName + " | " + downloadUrl);
         }
      }
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      String currentUrl = driver.getCurrentUrl();
      String pageNumber = currentUrl.substring(currentUrl.lastIndexOf("=") + 1);
      String newLink = "https://www.remixmp4.com/index" +
        ".php?action=&name=&artist=&price1=&price2=&order=&genre=&page=" +
        (Integer.parseInt(pageNumber) + 1);
      driver.get(newLink);
      Thread.sleep(500);
   }
}
