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
        
        String text = document.select("article[class=box_horizontal]").first().text();
        String firstDate = getDate(text);
        
        System.out.println(firstDate);
        
        String downloadDate = document
           .select("article[class=box_horizontal]")
           .stream()
           .map(element -> getDate(element.text()))
           .filter(date -> !date.equals(firstDate))
           .findFirst()
           .orElse(null);
        
        System.out.println(downloadDate);
        
        Elements trackInfos = document.select("article[class=box_horizontal]");
        for (Element trackInfo : trackInfos) {
            String trackDate = getDate(trackInfo.text());
            if (trackDate.equals(downloadDate)) {
                String trackName = trackInfo.select("h3").first().text();
                Element downloadInfo = trackInfo.select("a[class=link_add]").first();
                String[] typeAndID = downloadInfo.attr("onclick").replace("preview(", "")
                   .replace(")", "").split(",");
                String downloadUrl = MessageFormat.format(
                   "https://www.remixmp4.com/down.php?id={0}&type_={1}",
                   typeAndID[1], typeAndID[0]);
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
