package scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.FUtils;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScraperTest {
   
   public static void main(String[] args) throws ParseException {
      String html = FUtils.readFile(new File("Files/source.html"));
      Document document = Jsoup.parse(html);
      
      document.select("li[class=set]")
        .stream()
        .map(element -> element.select("div[class=set-title]"))
        .forEach(element -> System.out.println(
          element.text().toLowerCase().replaceAll(" ", "-")));
      
      // String firstDate = document
      //   .select("table[id=dl_table]>tbody>tr").first()
      //   .select("td").get(2)
      //   .text();
      //
      // System.out.println(firstDate);
      //
      // String downloadDate = document
      //   .select("table[id=dl_table]>tbody>tr")
      //   .stream()
      //   .map(element -> element.select("td").get(2).text())
      //   .filter(date -> !date.equals(firstDate))
      //   .findFirst()
      //   .orElse(null);
      //
      // System.out.println(downloadDate);
      // Elements trackInfos = Jsoup.parse(html).select("table[id=dl_table]>tbody>tr");
      // for (Element trackInfo : trackInfos) {
      //    String trackDate = trackInfo.select("td").get(2).text();
      //    if (trackDate.equals(downloadDate)) {
      //       String trackName = trackInfo.select("td").get(1).text();
      //       String downloadPartLink = trackInfo.select("td").get(1)
      //         .select("a").attr("href");
      //       String downloadUrl = "https://www.masspoolmp3.com" + downloadPartLink.replaceAll(" ", "%20");
      //       System.out.println(trackName + " | " + downloadUrl);
      //    }
      // }
   }
   
   private static int compareLists(List<String> scrapedLinks, List<String> oldScrape) {
      List<String> scrapedLinksTemp = new ArrayList<>(scrapedLinks);
      List<String> oldScrapeTemp = new ArrayList<>(oldScrape);
      
      int originalSize = scrapedLinksTemp.size();
      scrapedLinksTemp.removeAll(oldScrapeTemp);
      int changedPercent = (int) (scrapedLinksTemp.size() / ((float) originalSize / 100));
      System.out.println(changedPercent + "% Changed");
      return changedPercent;
   }
   
   public static void getStrings(List<String> scrapedLinks) {
      List<String> duplicates = scrapedLinks.stream()
        .filter(scrapedLink -> scrapedLink.endsWith("/"))
        .collect(Collectors.toList());
      scrapedLinks.removeAll(duplicates);
   }
}
