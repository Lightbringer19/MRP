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
      String html = FUtils.readFile(new File("Z:\\source.html"));
      Document document = Jsoup.parse(html);
      
      Element dateElement = document.select("td[class=footable-visible footable-last-column]")
        .first();
      String firstDate = dateElement.text().contains("Updated:")
        ? dateElement.select("div").get(1).text()
        : dateElement.select("div").first().text();
      
      System.out.println(firstDate);
      
      String downloadDate = document
        .select("td[class=footable-visible footable-last-column]")
        .stream()
        .map(trackInfo -> trackInfo.text().contains("Updated:")
          ? trackInfo.select("div").get(1).text()
          : trackInfo.select("div").first().text())
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
      
      System.out.println(downloadDate);
      
      Elements trackInfos = document.select("tbody>tr");
      for (Element trackInfo : trackInfos) {
         Element date = trackInfo
           .select("td[class=footable-visible footable-last-column]")
           .first();
         String trackDate = date.text().contains("Updated:")
           ? date.select("div").get(1).text()
           : date.select("div").first().text();
         if (trackDate.equals(downloadDate)) {
            String trackName =
              trackInfo.select("div[class=grid-text]").get(0).text() + " " +
                trackInfo.select("div[class=grid-text]").get(1).text();
            Elements downloadButtons = trackInfo.select("div[class=btn-group]>button");
            for (Element downloadButton : downloadButtons) {
               String videoId = MessageFormat.format(
                 "https://www.smashvision.net/Handlers/DownloadHandler.ashx?id={0}",
                 downloadButton.attr("id").replace("btn_", ""));
               System.out.println(videoId + " " + trackName);
            }
            
         }
      }
   }
   
}
