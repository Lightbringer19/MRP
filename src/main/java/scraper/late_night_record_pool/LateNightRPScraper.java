package scraper.late_night_record_pool;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import mongodb.MongoControl;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bson.Document;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import scraper.abstraction.ScrapingInterface;
import scraper.late_night_record_pool.LNRPApiResponse.LNRPRelease;
import utils.FUtils;
import utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

import static java.text.MessageFormat.format;
import static java.util.concurrent.ThreadLocalRandom.current;
import static utils.CheckDate.getCurrentYear;
import static utils.CheckDate.getTodayDate;
import static utils.Constants.tagsDir;

public class LateNightRPScraper extends Thread implements LNRPInterface, ScrapingInterface {
   private final String releaseName;
   private Logger logger;
   
   private final MongoCollection<Document> downloaded;
   private final MongoCollection<Document> downloadedTracks;
   List<LNRPRelease> releases;
   
   private static final String apiUrl =
     "https://crate.latenightrecordpool.com:2053/api/releases/0/50";
   
   public LateNightRPScraper() {
      MongoControl mongoControl = new MongoControl();
      downloaded = mongoControl.lateNightRPDownloaded;
      downloadedTracks = mongoControl.lateNightRPTracksDownloaded;
      releaseName = "Late Night Record Pool";
      logger = new Logger(releaseName);
   }
   
   @SneakyThrows
   public static void main(String[] args) {
      LateNightRPScraper lateNightRPScraper = new LateNightRPScraper();
      lateNightRPScraper.run();
   }
   
   @Override
   public void run() {
      Timer timer = new Timer();
      timer.schedule(new CheckTask(this, timer), 0);
   }
   
   @AllArgsConstructor
   class CheckTask extends TimerTask {
      LateNightRPScraper scraper;
      Timer timer;
      
      @Override
      public void run() {
         long sec = 1000;
         long min = sec * 60;
         long hour = 60 * min;
         scraper.check();
         timer.schedule(new CheckTask(scraper, timer),
           current().nextInt(60, 180) * min);
      }
   }
   
   private void check() {
      try {
         scrapingStage();
      } catch (Exception e) {
         logger.log(e);
      } finally {
      }
      
   }
   
   private void scrapingStage() {
      logger.log("Checking | Sending API Call");
      String responseFromApi = getResponseFromApi(apiUrl);
      releases = new Gson().fromJson(responseFromApi, LNRPApiResponse.class).getReleases();
      logger.log("Extracting Info from Response");
      String firstDate = getFirstDate();
      boolean newReleaseOnThePool = isNotInDb(firstDate, "releaseDate", downloaded);
      if (newReleaseOnThePool) {
         logger.log("Downloading New Release");
         //  Find next date
         String downloadDate = getDownloadDate(firstDate);
         // add to DB
         scrapeAndDownloadOperation(downloadDate);
         downloaded.insertOne(
           new Document("releaseDate", firstDate));
      }
   }
   
   public String getFirstDate() {
      return releases.stream()
        .filter(release -> release.getUpdated().equals("false"))
        .findFirst()
        .map(LNRPRelease::getAdded)
        .orElse(null);
   }
   
   public String getDownloadDate(String firstDate) {
      return releases
        .stream()
        .filter(release -> release.getUpdated().equals("false"))
        .map(LNRPRelease::getAdded)
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   private Map<String, Integer> scrapeTracksToDownload(String downloadDate) {
      
      Map<String, Integer> downloadMap = new LinkedHashMap<>();
      releases.stream()
        .filter(release ->
          release.getAdded().equals(downloadDate) || release.getUpdated().equals(downloadDate))
        .forEach(release ->
          release.getVersions()
            .forEach(version -> {
               if (isNotInDb(version.getId(), "trackID", downloadedTracks)) {
                  String type = editType(version);
                  String trackName = format("{0} - {1} {2}",
                    release.getArtist().trim(), release.getTitle().trim(), type);
                  downloadMap.put(trackName, version.getId());
               }
            }));
      return downloadMap;
   }
   
   private void scrapeAndDownloadOperation(String downloadDate) {
      Map<String, Integer> downloadMap = scrapeTracksToDownload(downloadDate);
      String formattedDownloadDate = formatDownloadDate(downloadDate, "MMM dd, yyyy");
      downloadLinks(downloadMap, releaseName + " " + formattedDownloadDate);
   }
   
   private void downloadLinks(Map<String, Integer> downloadLinksMap, String releaseName) {
      logger.log("Downloading release: " + releaseName);
      String releaseFolderPath =
        "E://TEMP FOR LATER/" + getCurrentYear() + "/" + getTodayDate() +
          "/RECORDPOOL/" + releaseName + "/";
      new File(releaseFolderPath).mkdirs();
      Scheduler scheduler = Schedulers.newParallel("Download", 15);
      String template = "https://crate.latenightrecordpool.com:2053/api/version/play/{0}";
      Flux.fromIterable(downloadLinksMap.entrySet())
        .parallel()
        .runOn(scheduler)
        .doOnNext(entry -> {
           downloadFile(format(template, entry.getValue().toString()), releaseFolderPath,
             entry.getKey() + ".mp3");
           downloadedTracks.insertOne(
             new Document("trackID", entry.getValue()).append("trackName", entry.getKey()));
        })
        .sequential()
        .blockLast();
      scheduler.dispose();
      logger.log("Release Downloaded: " + releaseName);
      FUtils.writeFile(tagsDir.replace("\\Scrapers", ""),
        releaseName + ".json",
        releaseFolderPath);
      logger.log("Release added to tag queue: " + releaseName);
   }
   
   private void downloadFile(String url, String releaseFolderPath, String fileName) {
      try {
         @Cleanup CloseableHttpClient client = HttpClients.createDefault();
         HttpGet get = new HttpGet(url);
         @Cleanup CloseableHttpResponse response = client.execute(get);
         String pathname = releaseFolderPath + "/" + fileName;
         File mp3File = new File(pathname);
         logger.log("Downloading: " + fileName + " | " + url
           + " | " + response.getStatusLine());
         @Cleanup OutputStream outputStream = new FileOutputStream(mp3File);
         response.getEntity().writeTo(outputStream);
         logger.log("Downloaded: " + fileName);
      } catch (Exception e) {
         logger.log(e);
      }
      
   }
}