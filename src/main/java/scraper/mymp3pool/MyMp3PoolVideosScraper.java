package scraper.mymp3pool;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;
import utils.Logger;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MyMp3PoolVideosScraper extends Scraper {
   
   public MyMp3PoolVideosScraper() {
      System.setProperty("jsse.enableSNIExtension", "false");
      USERNAME = yamlConfig.getMp3_pool_username();
      PASS = yamlConfig.getMp3_pool_password();
      dateFormat = "MMMM";
      loginUrl = "https://mp3poolonline.com/user/login";
      nameFieldNavigator = By.id("edit-name");
      passFieldNavigator = By.id("edit-pass");
      submitButtonNavigator = By.id("edit-submit");
      downloaded = mongoControl.mp3PoolDownloaded;
      releaseName = "MyMp3Pool Videos";
      checkOldReleases = false;
      logger = new Logger(releaseName);
   }
   
   public static void main(String[] args) {
      MyMp3PoolVideosScraper myMp3PoolScraper = new MyMp3PoolVideosScraper();
      myMp3PoolScraper.run();
   }
   
   @Override
   public void afterLoginStage() {
      driver.get("https://mp3poolonline.com/videoview");
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html)
        .select("div[class=month-divider]")
        .first().text().replace(" Videos", "");
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("div[class=month-divider]")
        .stream()
        .map(trackInfo -> trackInfo.text().replace(" Videos", ""))
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @SuppressWarnings("DuplicatedCode")
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
      String htmlWithTracks;
      int indexOfDownloadDate = html.indexOf(downloadDate);
      
      Optional<String> dateAfterDownloadDate = Jsoup.parse(html)
        .select("div[class=month-divider]")
        .stream()
        .map(trackInfo -> trackInfo.text().replace(" Videos", ""))
        .filter(date -> !date.equals(downloadDate) && !date.equals(firstDate))
        .findFirst();
      
      if (dateAfterDownloadDate.isPresent()) {
         int indexOfSecondDate = html.indexOf(dateAfterDownloadDate.get());
         htmlWithTracks = html.substring(indexOfDownloadDate, indexOfSecondDate);
      } else {
         htmlWithTracks = html.substring(indexOfDownloadDate);
      }
      
      Jsoup.parse(htmlWithTracks)
        .select("li[class*=views-row]")
        .stream()
        .map(element -> element.select("a").attr("href")
          .replace("https://mp3poolonline.com/node/", ""))
        .peek(s -> logger.log("Scraped: " + s))
        .forEach(scrapedLinks::add);
   }
   
   @Override
   @SneakyThrows
   public void operationWithLinksAfterScrape(List<String> scrapedLinks) {
      String downloadTemplate = "https://mp3poolonline.com/videos2/download/{0}";
      List<String> distinctList = scrapedLinks.stream().distinct()
        .map(s -> MessageFormat.format(downloadTemplate, s))
        .collect(Collectors.toList());
      scrapedLinks.clear();
      scrapedLinks.addAll(distinctList);
   }
   
   @Override
   @SneakyThrows
   protected String formatDownloadDate(String date) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date()); // get current year
      cal.set(Calendar.DAY_OF_MONTH, 1); // set to first day om the month
      cal.set(Calendar.MONTH, new SimpleDateFormat(dateFormat, Locale.US)
        .parse(date).getMonth()); // get download month
      if (date.equals("December")) {
         cal.add(Calendar.YEAR, -1);
      }
      return new SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.getTime());
   }
   
   @Override
   @SneakyThrows
   public void nextPage() {
      String currentUrl = driver.getCurrentUrl();
      if (currentUrl.contains("page")) {
         int pageNumber = Integer.parseInt(currentUrl.substring(currentUrl.indexOf("=") + 1));
         driver.get("https://mp3poolonline.com/videoview?page=" + (pageNumber + 1));
      } else {
         driver.get("https://mp3poolonline.com/videoview?page=1");
      }
      sleep(300);
   }
}
