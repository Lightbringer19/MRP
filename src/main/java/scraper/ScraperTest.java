package scraper;

import org.jetbrains.annotations.NotNull;
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
        String html = FUtils.readFile(new File("Z:\\source.html"));
        Document document = Jsoup.parse(html);
        
        String firstDate = document.select("small:contains(Creado el)")
           .first().text()
           .replace("- Creado el: ", "");
        
        System.out.println(firstDate);
        
        String downloadDate = document
           .select("small:contains(Creado el)")
           .stream()
           .map(Element::text)
           .map(date -> date.replace("- Creado el: ", ""))
           .filter(date -> !date.equals(firstDate))
           .findFirst()
           .orElse(null);
        
        System.out.println(downloadDate);
        
        Elements trackInfos = document.select("tr[class=table_product]");
        for (Element trackInfo : trackInfos) {
            String trackDate = trackInfo.select("small:contains(Creado el)")
               .first().text()
               .replace("- Creado el: ", "");
            if (trackDate.equals(downloadDate)) {
                String trackName = trackInfo.select("h3").first().text();
                String downloadID = trackInfo.attr("data-product_id");
                String downloadUrl = MessageFormat.format(
                   "https://maletadvj.com/products/descargar_producto/" +
                      "{0}", downloadID);
                System.out.println(trackName + " | " + downloadUrl);
            }
        }
        
    }
    
    @NotNull
    public static String getDate(String text) {
        int beginning = text.indexOf(" ", text.indexOf("Date: ")) + 1;
        int end = text.indexOf(" ", beginning);
        return text.substring(beginning, end);
    }
    
}
