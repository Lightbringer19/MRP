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
      String html = FUtils.readFile(new File("Files/source.html"));
      Document document = Jsoup.parse(html);
      
      String firstDate = document
        .select("div[class=col-created_at link]").first().text();
      
      System.out.println(firstDate);
      
      String downloadDate = document
        .select("div[class=col-created_at link]")
        .stream()
        .map(Element::text)
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
      
      System.out.println(downloadDate);
      
      String cssQuery = "div[class=row-item row-item-album audio ]";
      // if (!audioRelease) {
      //    cssQuery = "div[class=row-item  row-item-album video ]";
      // }
      Elements trackInfos = Jsoup.parse(html).select(
        cssQuery);
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("div[class=col-created_at link]").first()
           .text();
         if (trackDate.equals(downloadDate)) {
            String title = trackInfo.select("div[class=row-track]").text();
            Elements tags = trackInfo.select("div[class=row-tags]>span");
            for (Element tag : tags) {
               String trackId = tag.attr("id").replace("New Releases_media_tag_", "");
               // String linkForApi = format(
               //   "https://api.bpmlatino.com/v1/media/{0}/download?crate=false", trackId);
               // List<String> info = getDownloadInfo(linkForApi, "latino");
               // String downloadUrl = info.get(0);
               String trackType = tag.text();
               System.out.println(title + " (" + trackType + ") | "
                 + trackId);
            }
         }
      }
      
   }
}
