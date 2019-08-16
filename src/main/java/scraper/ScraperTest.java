package scraper;

import org.jetbrains.annotations.NotNull;
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
        
        String firstDate = document.select("time").first().text();
        
        System.out.println(firstDate);
        
        String downloadDate = document
           .select("time")
           .stream()
           .map(Element::text)
           .filter(date -> !date.equals(firstDate))
           .findFirst()
           .orElse(null);
        
        System.out.println(downloadDate);
        
        String subHtml = html.substring(html.indexOf(downloadDate), html.indexOf(document
           .select("time")
           .stream()
           .map(Element::text)
           .filter(date -> !date.equals(firstDate) && !date.equals(downloadDate))
           .findFirst()
           .orElse("<nav class=\"navigation pagination\" role=\"navigation\">")));
        Elements trackInfos = Jsoup.parse(subHtml).select("li[class*=row results]");
        for (Element trackInfo : trackInfos) {
            Elements downloadButtons = trackInfo.select("option[class*=player--version]");
            for (Element downloadButton : downloadButtons) {
                String downloadID = downloadButton.attr("data-id");
                String trackName = downloadButton.attr("data-f");
                String urlForApi = "https://www.heavyhits.com/hh18/library/download.php?id=" + downloadID;
                String downloadUrl;
                // System.out.println(trackName + " | " + downloadUrl);
            }
            
            // System.out.println(trackInfo.text());
        }
        
        // Elements trackInfos = document.select("tr[id*=singleSongPlayer]");
        // for (Element trackInfo : trackInfos) {
        //     String trackDate = trackInfo.select("td").first().text();
        //     if (trackDate.equals(downloadDate)) {
        //         String trackName = trackInfo.text();
        //         String downloadID = trackInfo.attr("data-product");
        //         String downloadUrl = MessageFormat.format(
        //            "https://dalemasbajo.com/products/descargar_producto/{0}",
        //            downloadID);
        //         System.out.println(trackName + " | " + downloadUrl);
        //     }
        // }
        //
    }
    
    @NotNull
    public static String getDate(String text) {
        int beginning = text.indexOf(" ", text.indexOf("Date: ")) + 1;
        int end = text.indexOf(" ", beginning);
        return text.substring(beginning, end);
    }
    
}
