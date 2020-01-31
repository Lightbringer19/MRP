package scraper.clubdj;

import com.google.gson.Gson;
import lombok.Data;
import lombok.SneakyThrows;
import org.bson.Document;
import org.openqa.selenium.By;
import scraper.abstraction.Scraper;
import scraper.clubdj.ClubDJScraper.FullInfo.VideosBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static utils.ApiInterface.getClean;

public class ClubDJScraper extends Scraper {
   
   private List<VideosBean> videos;
   
   public ClubDJScraper() {
   
      USERNAME = yamlConfig.getClubdj_username();
      PASS = yamlConfig.getClubdj_password();
      dateFormat = "dd-MM-yyyy";
      loginUrl = "http://www.clubdjvideos.com/index.html#/login";
      nameFieldNavigator = By.xpath("/html/body/div[2]/div[2]/div/div/div[6]/input");
      passFieldNavigator = By.xpath("/html/body/div[2]/div[2]/div/div/div[10]/input");
      submitButtonNavigator = By.xpath("/html/body/div[2]/div[2]/div/div/button");
      downloaded = mongoControl.clubDjVideosDownloaded;
      releaseName = "ClubDJ Videos";
      
   }
   
   public static void main(String[] args) {
      ClubDJScraper clubDJScraper = new ClubDJScraper();
      clubDJScraper.start();
   }
   
   @Override
   public void beforeLogin() {
      driver.findElement(By.xpath("/html/body/div/a")).click();
      driver.get(loginUrl);
   }
   
   @Override
   @SneakyThrows
   public void afterLoginStage() {
      sleep(2500);
      setCookieForAPI();
      String response = getClean("http://www.clubdjvideos.com/videos/set/first?ascending=false&totalSize=1000&index=1&sortField=dateAdded&viewableSize=300",
        cookieForAPI);
      videos = new Gson().fromJson(response, FullInfo.class).getVideos();
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return new SimpleDateFormat(dateFormat)
        .format(videos.get(0).getDateAdded());
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return videos
        .stream()
        .map(video -> new SimpleDateFormat(dateFormat).format(video.getDateAdded()))
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   protected List<String> scrapeLinks(String firstDate, String downloadDate) {
      List<String> scrapedLinks = new ArrayList<>();
      for (VideosBean video : videos) {
         String trackDate = new SimpleDateFormat(dateFormat)
           .format(video.getDateAdded());
         if (trackDate.equals(downloadDate)) {
            String requestUrl =
              "http://www.clubdjvideos.com/download-info?videoId=" + video.getId();
            String purchaseInfo = getClean(requestUrl, cookieForAPI);
            if (purchaseInfo != null) {
               String downloadLink = (String) Document.parse(purchaseInfo).get("downloadLink");
               String trackName = video.getTitle();
               logger.log(downloadLink + " " + trackName);
               logger.log("CREDITS LEFT: " + ((Document) Document.parse(purchaseInfo)
                 .get("user")).get("downloadCredit").toString());
               scrapedLinks.add(downloadLink);
            }
         }
      }
      return scrapedLinks;
   }
   
   @Data
   public static class FullInfo {
      
      private ListPositionBean listPosition;
      private List<VideosBean> videos;
      
      @Data
      public static class ListPositionBean {
         private Object title;
         private Object artist;
         private Object genre;
         private Object year;
         private Object search;
         private int daysPrior;
         private boolean subscribed;
         private boolean ascending;
         private int index;
         private int viewableSize;
         private int totalSize;
         private String sortField;
         private int totalSegments;
         private int offset;
         private List<Integer> segments;
      }
      
      @Data
      public static class VideosBean {
         private int id;
         private String filename;
         private String title;
         private String description;
         private String artist;
         private String genre;
         private int itemsold;
         private int bps;
         private String prevfile;
         private long dateAdded;
         private String resolution;
         private int version;
         private int downloadCount;
         private String date;
         
      }
   }
}
