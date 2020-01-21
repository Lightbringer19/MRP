package scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.FUtils;

import java.io.File;
import java.text.ParseException;
import java.util.Optional;

public class ScraperTest {
   
   public static void main(String[] args) throws ParseException {
      String html = FUtils.readFile(new File("Files/source.html"));
      Document document = Jsoup.parse(html);
      
      String firstDate = document
        .select("div[class=tracks_homepage]>div>div")
        .first().text()
        .replace("Added on ", "");
      
      System.out.println(firstDate);
      
      String downloadDate = document
        .select("div[class=date-added]")
        .stream()
        .map(nextDate -> nextDate.text().replace("Added on ", ""))
        .filter(dateFormatted -> !dateFormatted.equals(firstDate))
        .findFirst()
        .orElse(null);
      
      System.out.println(downloadDate);
      
      Elements dates = document.select("div[class=date-added]");
      String containerHtml = document.select("div[class=tracks_homepage]").html();
      int indexOfFirstDate = containerHtml.indexOf(downloadDate);
      
      Optional<String> dateAfterDownloadDate = dates.stream()
        .map(date -> date.text().replace("Added on ", ""))
        .filter(dateFormatted ->
          !dateFormatted.equals(downloadDate) && !dateFormatted.equals(firstDate))
        .findFirst();
      String htmlWithTracks;
      if (dateAfterDownloadDate.isPresent()) {
         int indexOfSecondDate = containerHtml.indexOf(dateAfterDownloadDate.get());
         htmlWithTracks = containerHtml.substring(indexOfFirstDate, indexOfSecondDate);
      } else {
         htmlWithTracks = containerHtml.substring(indexOfFirstDate);
      }
      
      Jsoup.parse(htmlWithTracks).select("li[class*=post-view load-tracks]").stream()
        .map(trackInfo ->
          trackInfo.select("div[class*=download-stars]>a").attr("href"))
        .forEach(System.out::println);
      
      // String template = "https://headlinermusicclub.com/?get_file={0}";
      // Jsoup.parse(htmlWithTracks).select("li[class*=post-view load-tracks]")
      //   .forEach(trackInfo ->
      //     trackInfo.select("a:contains(download)").stream()
      //       .map(element -> MessageFormat.format(template, element.attr("data-file")))
      //       .forEach(System.out::println));
      
   }
}
