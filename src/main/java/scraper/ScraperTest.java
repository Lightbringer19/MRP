package scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.FUtils;

import java.io.File;
import java.text.MessageFormat;
import java.text.ParseException;

public class ScraperTest {
   
   public static void main(String[] args) throws ParseException {
      String html = FUtils.readFile(new File("GDS_SERVER/source.html"));
      Document document = Jsoup.parse(html);
      
      String firstDate = document
        .select("table[id=dl_table]>tbody>tr").first()
        .select("td").get(2)
        .text();
      
      System.out.println(firstDate);
      
      String downloadDate = document
        .select("table[id=dl_table]>tbody>tr")
        .stream()
        .map(element -> element.select("td").get(2).text())
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
      
      System.out.println(downloadDate);
      
      Elements trackInfos = Jsoup.parse(html).select("table[id=dl_table]>tbody>tr");
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("td").get(2).text();
         if (trackDate.equals(downloadDate)) {
            String trackName = trackInfo.select("td").get(1).text();
            String downloadPartLink = trackInfo.select("td").get(1)
              .select("a").attr("href");
            String downloadUrl = MessageFormat.format(
              "http://www.masspoolmp3.com" +
                "{0}", downloadPartLink);
            String downloadLink = (downloadUrl);
            System.out.println(trackName + " | " + downloadLink);
         }
      }
   }
   
}
