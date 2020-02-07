package scraper;

import com.mongodb.client.MongoCollection;
import mongodb.MongoControl;
import org.bson.Document;
import org.jsoup.Jsoup;
import utils.FUtils;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

public class ScraperTest {
   
   public static void main(String[] args) throws ParseException {
      String html = FUtils.readFile(new File("Files/source.html"));
      // Document document = Jsoup.parse(html);
      //
      // String firstDate = document
      //   .select("div[class=month-divider]")
      //   .first().text().replace(" Videos", "");
      //
      // System.out.println(firstDate);
      //
      // String downloadDate = document
      //   .select("div[class=month-divider]")
      //   .stream()
      //   .map(trackInfo -> trackInfo.text().replace(" Videos", ""))
      //   .filter(date -> !date.equals(firstDate))
      //   .findFirst()
      //   .orElse(null);
      
      // System.out.println(downloadDate);
      
      String downloadDate = null;
      
      // String firstDate = "";
      List<String> scrapedLinks = new ArrayList<>();
      Jsoup.parse(html)
        .select("div[class=innerPlayer1]")
        .stream()
        .filter(release -> downloadDate == null || release.select("p").first().text()
          .replace("Added On: ", "").equals(downloadDate))
        .forEach(release -> release.select("div>ul>li")
          .forEach(track -> {
             track.select("div[class=track-title]").text();
             track.select("div[class=download2 sub-section]>a")
               .stream()
               .map(link -> link.attr("href"))
               .filter(downloadUrl -> downloadUrl.contains("download/"))
               .forEach(scrapedLinks::add);
          }));
      
      getStrings(scrapedLinks);
      
      List<String> oldScrape = new ArrayList<>();
      for (int i = 0; i < scrapedLinks.size(); i++) {
         String s = scrapedLinks.get(i);
         if (i < 55) {
            oldScrape.add(s + "%TSASDDASDFASDFA");
         } else {
            oldScrape.add(s);
         }
      }
      // scrapedLinks.clear();
      // scrapedLinks.addAll(oldScrape);
      // compareLists(scrapedLinks, oldScrape);
      
      MongoControl mongoControl = new MongoControl();
      MongoCollection<Document> downloaded = mongoControl.mp3PoolDownloaded;
      
      String playlistName = "TEST";
      Document playlistInDb = downloaded.find(eq("playlistName", playlistName)).first();
      if (playlistInDb != null) {
         List<String> scrapedLinksInDb = (List<String>) playlistInDb.get("scrapedLinks");
         int changedPercent = compareLists(scrapedLinks, scrapedLinksInDb);
         if (changedPercent > 50) {
            System.out.println("UPDATED");
            playlistInDb.put("scrapedLinks", scrapedLinks);
            downloaded.findOneAndReplace(eq("playlistName", playlistName), playlistInDb);
            // downloaded.insertOne(playlistInDb);
         }
      } else {
         System.out.println("INSERTED NEW");
         downloaded.insertOne(new Document()
           .append("playlistName", playlistName)
           .append("scrapedLinks", scrapedLinks));
      }
   }
   
   private static int compareLists(List<String> scrapedLinks, List<String> oldScrape) {
      List<String> scrapedLinksTemp = new ArrayList<>(scrapedLinks);
      List<String> oldScrapeTemp = new ArrayList<>(oldScrape);
      
      int originalSize = scrapedLinksTemp.size();
      scrapedLinksTemp.removeAll(oldScrapeTemp);
      int changedPercent = (int) (scrapedLinksTemp.size() / ((float) originalSize / 100));
      System.out.println(changedPercent + "% Changed");
      return changedPercent;
   }
   
   public static void getStrings(List<String> scrapedLinks) {
      List<String> duplicates = scrapedLinks.stream()
        .filter(scrapedLink -> scrapedLink.endsWith("/"))
        .collect(Collectors.toList());
      scrapedLinks.removeAll(duplicates);
   }
}
