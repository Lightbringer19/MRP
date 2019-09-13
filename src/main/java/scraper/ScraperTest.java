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
      
      String firstDate = document.select("tbody>tr").first()
        .select("td").first().text();
      
      System.out.println(firstDate);
      
      String downloadDate = document
        .select("tbody>tr")
        .stream()
        .map(trackInfo -> trackInfo.select("td").first().text())
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
      
      System.out.println(downloadDate);
      
      Elements trackInfos = document.select("tbody>tr");
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("td").first().text();
         if (trackDate.equals(downloadDate)) {
            for (Element downloadInfo : trackInfo
              .select("td[class=catalog-download hidden-xs]>a")) {
               String trackName = downloadInfo.attr("title");
               String downloadCode = downloadInfo.attr("data-code");
               String downloadUrl = downloadInfo.attr("href");
               System.out.println(downloadUrl + " " + trackName);
            }
            
         }
      }
   }
   
}
