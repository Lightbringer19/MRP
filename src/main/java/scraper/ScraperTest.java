package scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.FUtils;

import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

public class ScraperTest {
   
   public static void main(String[] args) throws ParseException {
      String html = FUtils.readFile(new File("Files/source.html"));
      Document document = Jsoup.parse(html);
      
      String firstDate = document
        .select("span[id=updated_date]")
        .first().text()
        .replace("updated ", "");
      
      System.out.println(firstDate);
      
      List<String> links = Jsoup.parse(html).select("li[class*=post-view load-tracks]").stream()
        .map(trackInfo ->
          trackInfo.select("div[class*=download-stars]>a").attr("href"))
        .collect(Collectors.toList());
      
      //    String downloadDate = document
      //      .select("div[class=date-added]")
      //      .stream()
      //      .map(nextDate -> nextDate.text().replace("Added on ", ""))
      //      .filter(dateFormatted -> !dateFormatted.equals(firstDate))
      //      .findFirst()
      //      .orElse(null);
      //
      //    System.out.println(downloadDate);
      //
      //    Elements dates = document.select("div[class=date-added]");
      //    String containerHtml = document.select("div[class=tracks_homepage]").html();
      //    int indexOfFirstDate = containerHtml.indexOf(downloadDate);
      //
      //    Optional<String> dateAfterDownloadDate = dates.stream()
      //      .map(date -> date.text().replace("Added on ", ""))
      //      .filter(dateFormatted ->
      //        !dateFormatted.equals(downloadDate) && !dateFormatted.equals(firstDate))
      //      .findFirst();
      //    String htmlWithTracks;
      //    if (dateAfterDownloadDate.isPresent()) {
      //       int indexOfSecondDate = containerHtml.indexOf(dateAfterDownloadDate.get());
      //       htmlWithTracks = containerHtml.substring(indexOfFirstDate, indexOfSecondDate);
      //    } else {
      //       htmlWithTracks = containerHtml.substring(indexOfFirstDate);
      //    }
      //
      //    Jsoup.parse(htmlWithTracks).select("li[class*=post-view load-tracks]").stream()
      //      .map(trackInfo ->
      //        trackInfo.select("div[class*=download-stars]>a").attr("href"))
      //      .forEach(System.out::println);
   }
}
