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
        
        String firstDate = document.select("td[class=created_at text-center]").first().text();
        
        System.out.println(firstDate);
        
        String downloadDate = document
           .select("td[class=created_at text-center]")
           .stream()
           .map(Element::text)
           .filter(date -> !date.equals(firstDate))
           .findFirst()
           .orElse(null);
        
        System.out.println(downloadDate);
        
        Elements trackInfos = document.select("tr[class=cc-song song ]");
        for (Element trackInfo : trackInfos) {
            String trackDate = trackInfo.select("td[class=created_at text-center]").first().text();
            if (trackDate.equals(downloadDate)) {
                String trackName = trackInfo.text();
                String downloadID = trackInfo.attr("data-id");
                String downloadUrl = MessageFormat.format(
                   "https://crateconnect.net/index.php?option=com_crateconnect&format=raw&task=downloadZipFile&fileid={0}",
                   downloadID);
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
