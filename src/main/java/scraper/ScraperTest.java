package scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.FUtils;

import java.io.File;
import java.text.ParseException;
import java.util.stream.Collectors;

public class ScraperTest {
   
   public static void main(String[] args) throws ParseException {
      String html = FUtils.readFile(new File("Files/source.html"));
      Document document = Jsoup.parse(html);
      
      String firstDate = document
        .select("tr[class*=tr_box]>td")
        .first().text();
      
      System.out.println(firstDate);
      
      String downloadDate = document
        .select("tr[class*=tr_box]")
        .stream()
        .map(trackInfo -> trackInfo.select("td").first().text())
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
      
      System.out.println(downloadDate);
      
      // TODO: 02.02.2020
      
      document
        .select("tr[class*=tr_box]")
        .stream()
        .filter(element -> element.select("td").first().text().equals(downloadDate))
        .map(element -> "http://www.latinvideoremix.com/" +
          element.select("a").last().attr("href")
            .replace("Javascript:frame('", "")
            .replace("');", ""))
        .collect(Collectors.toList());
      String downloadLink = "http://www.latinvideoremix.com/download_start.php?file=67049";
   }
}
