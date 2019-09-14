package scraper;

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
      
      String firstDate = document.select("div[class=data]").first()
        .select("strong").first().text()
        .replace("New Version Added: ", "");
      
      System.out.println(firstDate);
      
      String downloadDate = document
        .select("div[class=data]")
        .stream()
        .map(trackInfo -> trackInfo.select("strong").first().text()
          .replace("New Version Added: ", ""))
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
      
      System.out.println(downloadDate);
      
      Elements trackInfos = document.select(
        "div[class=content_data_downloads]>div[id*=id]");
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("div[class=data]").first()
           .select("strong").first().text()
           .replace("New Version Added: ", "");
         if (trackDate.equals(downloadDate)) {
            String linkForRequest = trackInfo.select("a").first().attr("href");
            System.out.println(linkForRequest);
            String trackName = trackInfo.select("img").first().attr("alt");
            // String downloadUrl = downloadInfo.attr("href");
            // System.out.println(downloadUrl + " " + trackName);
            System.out.println(trackName);
         }
      }
   }
   
}
