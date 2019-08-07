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
        String html = FUtils.readFile(new File("Z:\\source.html"));
        Document document = Jsoup.parse(html);
        
        String firstDate = document.select("div[class=date_box]").first().text();
        
        System.out.println(firstDate);
        
        String downloadDate = document
           .select("div[class=date_box]")
           .stream()
           .filter(date -> !date.text().equals(firstDate))
           .findFirst()
           .map(Element::text)
           .orElse(null);
        
        System.out.println(downloadDate);
        
        Elements trackInfos = document.select("ul[class=songlist]>li");
        for (Element trackInfo : trackInfos) {
            String trackDate = trackInfo.select("div[class=date_box]").text();
            if (trackDate.equals(downloadDate)) {
                Elements downloadInfos = trackInfo
                   .select("div[class=view_drop_block]>ul>li");
                for (Element downloadInfo : downloadInfos) {
                    String trackName = downloadInfo.select("p").text();
                    String downloadUrl = downloadInfo
                       .select("span[class=download_icon sprite ]>a")
                       .attr("href");
                    System.out.println(trackName + " | " + downloadUrl);
                }
            }
        }
        
    }
    
}
