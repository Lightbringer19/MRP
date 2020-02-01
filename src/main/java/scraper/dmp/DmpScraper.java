package scraper.dmp;

import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Data;
import mongodb.MongoControl;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.CheckDate;
import utils.FUtils;
import utils.Log;
import utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static scraper.dmp.DmpApiService.getDownloadUrl;
import static scraper.dmp.DmpDriver.cookieForAPI;
import static utils.Constants.tagsDir;

class DmpScraper {
   
   private static MongoControl mongoControl = new MongoControl();
   private static String date;
   
   static void checkForNewRelease(String html)
     throws IOException, ParseException, java.text.ParseException {
      ScrapedDmpPage scrapedDmpPage = scrapePageForDatesAndLinks(html);
      List<String> dates = scrapedDmpPage.getDates();
      String newDate = dates.get(0);
      boolean newReleaseNotInDB = mongoControl.dmpDownloaded
        .find(eq("releaseDate", newDate)).first() == null;
      if (newReleaseNotInDB) {
         Log.write("Found New Release: " + newDate, "DMP Scraper");
         mongoControl.dmpDownloaded.insertOne(new org.bson.Document("releaseDate", newDate));
         String dateForDownloadRaw = dates.get(1);
         Collection<String> linksToDownload = scrapedDmpPage
           .getScraped().get(dateForDownloadRaw);
         Log.write("Scraping Previous Release: " + dateForDownloadRaw, "DMP Scraper");
         // GET URLS VIA API
         List<String> downloadURLS = getDownloadUrls(linksToDownload);
         // DOWNLOAD RELEASE FROM PREVIOUS DATE
         downloadRelease(getDateForDmpDownload(dateForDownloadRaw), downloadURLS);
      }
   }
   
   private static ScrapedDmpPage scrapePageForDatesAndLinks(String html) {
      Document document = Jsoup.parse(html);
      MultiValuedMap<String, String> scraped = new HashSetValuedHashMap<>();
      Element table = document.select("table[id=SongTableData]").first();
      Elements trElements = table.select("tr");
      List<String> dates = new ArrayList<>();
      for (Element track : trElements) {
         if (track.select("td[class=SongTableDataRow]").hasText()) {
            date = track.text();
            dates.add(date);
         }
         if (track.className().equals("SongTableOdd") || track.className().equals("SongTableEven")) {
            String trackName = track.text();
            Elements links = track.select("td[class=ctr]");
            for (Element img : links) {
               String imgHtml = img.html();
               if (imgHtml.contains("openDetermineDownloadDialog")) {
                  int index = imgHtml.indexOf("'");
                  int lastIndex = imgHtml.lastIndexOf("'");
                  String scrapedInfoForDownload = imgHtml.substring(index + 1, lastIndex);
                  scraped.put(date, scrapedInfoForDownload);
               }
            }
         }
      }
      return new ScrapedDmpPage(scraped, dates);
   }
   
   private static List<String> getDownloadUrls(Collection<String> links)
     throws IOException, ParseException {
      List<String> downloadURLS = new ArrayList<>();
      for (String link : links) {
         Log.write("Will Scrape link from: " + link, "DMP Scraper");
         String downloadURL = getDownloadUrl(link, cookieForAPI);
         if (downloadURL != null) {
            downloadURLS.add(downloadURL);
         }
      }
      return downloadURLS;
   }
   
   private static void downloadRelease(String dateForReleaseName, List<String> downloadURLS) {
      String releaseName = "Digital Music Pool " + dateForReleaseName;
      new Logger("DMP Scraper").log("Downloading release: " + releaseName);
      String releaseFolderPath =
        "E://TEMP FOR LATER/2019/" + CheckDate.getTodayDate() +
          "/RECORDPOOL/" + releaseName + "/";
      new File(releaseFolderPath).mkdirs();
      Scheduler scheduler = Schedulers.newParallel("Download", 15);
      Flux.fromIterable(downloadURLS)
        .parallel()
        .runOn(scheduler)
        .doOnNext(downloadUrl -> downloadFile(downloadUrl, releaseFolderPath))
        .sequential()
        .blockLast();
      scheduler.dispose();
      new Logger("DMP Scraper").log("Release Downloaded: " + releaseName);
      FUtils.writeFile(tagsDir.replace("\\Scrapers", ""),
        releaseName + ".json", releaseFolderPath);
      new Logger("DMP Scraper").log("Release Scheduled: " + releaseName);
   }
   
   private static void downloadFile(String url, String releaseFolderPath) {
      try {
         String downloadUrl = url.replaceAll(" ", "%20")
           .replaceAll("\"", "%22");
         @Cleanup CloseableHttpClient client = HttpClients.createDefault();
         HttpGet get = new HttpGet(downloadUrl);
         get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
         @Cleanup CloseableHttpResponse response = client.execute(get);
         System.out.println(downloadUrl + " " + response.getStatusLine().getStatusCode());
         String fileName = response.getFirstHeader("Content-Disposition").getValue()
           .replace("attachment; filename=", "")
           .replace("attachment", "")
           .replace("filename=", "")
           .replaceAll(";", "")
           .replaceAll("\"", "")
           .replaceAll("\\\\", "")
           .replaceAll("&amp;", "&");
         File mp3File = new File(releaseFolderPath + fileName);
         new Logger("DMP Scraper").log("Downloading file: " + fileName + " | " + downloadUrl
           + " | " + response.getStatusLine());
         @Cleanup OutputStream outputStream = new FileOutputStream(mp3File);
         response.getEntity().writeTo(outputStream);
         new Logger("DMP Scraper").log("Downloaded: " + fileName);
      } catch (Exception e) {
         new Logger("DMP Scraper").log(e);
      }
   }
   
   private static String getDateForDmpDownload(String date) throws java.text.ParseException {
      SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
      Calendar cal = Calendar.getInstance();
      cal.setTime(DATE_FORMAT.parse(date));
      cal.add(Calendar.DAY_OF_MONTH, 1);
      System.out.println(DATE_FORMAT.format(cal.getTime()));
      return new SimpleDateFormat("ddMM").format(cal.getTime());
   }
   
   @Data
   @AllArgsConstructor
   static class ScrapedDmpPage {
      MultiValuedMap<String, String> scraped;
      List<String> dates;
   }
   
}
