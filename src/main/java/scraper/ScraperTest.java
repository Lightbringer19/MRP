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
      
      Elements trackInfos = document
        .select("div[class*=row-item-album]");
      for (Element trackInfo : trackInfos) {
         String date = trackInfo.select("div[class=col-created_at link]").text();
         if (date.equals(downloadDate)) {
            String title = trackInfo.select("div[class=row-track-name]").first().text();
            String artist = trackInfo.select("div[class=row-artist]").first().text();
            Elements tags = trackInfo.select("div[class=row-tags]").first()
              .select("span[class=tag-link]");
            for (Element tagInfo : tags) {
               
               String trackId = tagInfo.attr("id")
                 .replace("New Releases_media_tag_", "");
               String linkForApi = MessageFormat.format(
                 "https://api.bpmsupreme.com/v1.2/media/{0}/download?crate=false",
                 trackId);
               
               //+construct download url from artist title and tag name
               // String downloadUrl = MessageFormat.format(
               //   "https://av.bpmsupreme.com/audio/{0} - {1} ({2}).mp3?download",
               //   artist, title, tagInfo.text());
               // System.out.println(downloadUrl);
               //-construct download url from artist title and tag name
               
               // List<String> info = getDownloadInfo(linkForApi, "");
               // String downloadUrl = info.get(0);
               // cookieForAPI = info.get(1);
               // String trackType = tagInfo.text();
               // logger.log(
               //   MessageFormat.format(
               //     "{0} - {1} ({2}) | {3}",
               //     artist, title, tagInfo.text(), downloadUrl);
               // scrapedLinks.add(downloadUrl);
            }
         }
      }
      
   }
}
