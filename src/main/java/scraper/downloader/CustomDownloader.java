package scraper.downloader;

import mongodb.MongoControl;
import org.bson.Document;
import scraper.abstraction.DownloadInterface;
import utils.Logger;

import java.util.List;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;

public class CustomDownloader implements DownloadInterface {
   
   private static final Scanner IN = new Scanner(System.in);
   private static String COOKIE;
   private Logger logger = new Logger("Custom Downloader");
   
   public static void main(String[] args) {
      MongoControl mongoControl = new MongoControl();
      CustomDownloader customDownloader = new CustomDownloader();
      while (true) {
         System.out.println("Enter release name: ");
         String releaseName = IN.nextLine();
         if (releaseName.contains("MyMp3Pool")) {
            System.setProperty("jsse.enableSNIExtension", "false");
         }
         System.out.println("Enter cookies(press enter to reuse cookie): ");
         String cookie = IN.nextLine();
         if (!cookie.equals("")) {
            COOKIE = cookie;
         }
         Document releaseInfo = mongoControl.scrapedReleases
           .find(eq("releaseName", releaseName)).first();
         List<String> scrapedLinks = (List<String>) releaseInfo.get("scrapedLinks");
         customDownloader.downloadLinks(scrapedLinks, releaseName);
         if (releaseName.contains("MyMp3Pool")) {
            System.setProperty("jsse.enableSNIExtension", "true");
         }
      }
   }
   
   @Override
   public String getCookie() {
      return COOKIE;
   }
   
   @Override
   public Logger getLogger() {
      return logger;
   }
}
