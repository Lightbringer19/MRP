package scraper;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.FUtils;

import java.io.File;
import java.text.ParseException;

public class ScraperTest {
   
   public static void main(String[] args) throws ParseException {
      String html = FUtils.readFile(new File("Z:\\source.html"));
      Document document = Jsoup.parse(html);
      
      String firstDate = document.select("tbody>tr>td")
        .get(5).text();
      
      System.out.println(firstDate);
      
      String downloadDate = document
        .select("tbody>tr")
        .stream()
        .map(trackInfo -> trackInfo.select("td").get(5).text())
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
      
      System.out.println(downloadDate);
      
      Elements trackInfos = document.select("tbody>tr");
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("td").get(5).text();
         if (trackDate.equals(downloadDate)) {
            String trackName = trackInfo.select("td").get(1).text() +
              trackInfo.select("td").get(2).text();
            String videoId = trackInfo.select("td").get(0)
              .select("a").attr("data-videoid");
            System.out.println(videoId + " " + trackName);
            // System.out.println(trackName + " | " + downloadUrl);
         }
      }
      
   }
   
   @NotNull
   public static String getDate(String text) {
      int beginning = text.indexOf(" ", text.indexOf("Date: ")) + 1;
      int end = text.indexOf(" ", beginning);
      return text.substring(beginning, end);
   }
   
}
