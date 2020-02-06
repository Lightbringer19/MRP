package scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.FUtils;

import java.io.File;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScraperTest {
   
   public static void main(String[] args) throws ParseException {
      String html = FUtils.readFile(new File("Files/source.html"));
      Document document = Jsoup.parse(html);
      
      String firstDate = document
        .select("div[class=month-divider]")
        .first().text().replace(" Videos", "");
      
      System.out.println(firstDate);
      
      String downloadDate = document
        .select("div[class=month-divider]")
        .stream()
        .map(trackInfo -> trackInfo.text().replace(" Videos", ""))
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
      
      System.out.println(downloadDate);
      
      // String downloadDate = "December";
      // String firstDate = "";
      List<String> scrapedLinks = new ArrayList<>();
      String htmlWithTracks;
      int indexOfDownloadDate = html.indexOf(downloadDate);
      
      Optional<String> dateAfterDownloadDate = document
        .select("div[class=month-divider]")
        .stream()
        .map(trackInfo -> trackInfo.text().replace(" Videos", ""))
        .filter(date -> !date.equals(downloadDate) && !date.equals(firstDate))
        .findFirst();
      
      if (dateAfterDownloadDate.isPresent()) {
         int indexOfSecondDate = html.indexOf(dateAfterDownloadDate.get());
         htmlWithTracks = html.substring(indexOfDownloadDate, indexOfSecondDate);
      } else {
         htmlWithTracks = html.substring(indexOfDownloadDate);
      }
      
      Jsoup.parse(htmlWithTracks)
        .select("li[class*=views-row]")
        .stream()
        .map(element -> element.select("a").attr("href")
          .replace("https://mp3poolonline.com/node/", ""))
        .forEach(scrapedLinks::add);
      
      getStrings(scrapedLinks);
      System.out.println(scrapedLinks);
   }
   
   public static void getStrings(List<String> scrapedLinks) {
      String downloadTemplate = "https://mp3poolonline.com/videos2/download/{0}";
      List<String> distinctList = scrapedLinks.stream().distinct()
        .map(s -> MessageFormat.format(downloadTemplate, s))
        .collect(Collectors.toList());
      scrapedLinks.clear();
      scrapedLinks.addAll(distinctList);
   }
}
