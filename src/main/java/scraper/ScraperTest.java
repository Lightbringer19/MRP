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
      String html = FUtils.readFile(new File("source.html"));
      Document document = Jsoup.parse(html);
      
      String firstDate = document.select("div[class=col-created_at]").first()
        .text();
      
      System.out.println(firstDate);
      
      String downloadDate = document
        .select("div[class=col-created_at]")
        .stream()
        .map(Element::text)
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
      
      System.out.println(downloadDate);
      
      Elements trackInfos = document.select(
        "div[class=row-item row-item-album audio ]");
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("div[class=col-created_at]").first()
           .text();
         if (trackDate.equals(downloadDate)) {
            String title = trackInfo.select("div[class=row-track]").text();
            Elements tags = trackInfo.select("div[class=row-tags]>span");
            for (Element tag : tags) {
               String id = tag.attr("id").replace("New Releases_media_tag_", "");
               String trackType = tag.text();
               System.out.println(title + " (" + trackType + ") | "
                 + id);
            }
         }
      }
   }
   
}
