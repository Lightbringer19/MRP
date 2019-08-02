package scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.FUtils;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ScraperTest {
    
    private static String htmlWithTracks;
    
    public static void main(String[] args) throws ParseException {
        String html = FUtils.readFile(new File("Z:\\source.html"));
        Document document = Jsoup.parse(html);
        
        List<String> scrapedLinks = new ArrayList<>();
        String downloadDate = "08/01/19";
        // TODO: 02.08.2019 Finish Scraping
        Elements trackInfos = document.select("li[class=even updatedversion ng-scope]");
        for (Element trackInfo : trackInfos) {
            String date = trackInfo.select("span[class=date ng-binding]").text();
            if (date.equals(downloadDate)) {
                Element trackTags = trackInfo.select("div[class=tag]").first();
                Elements tracks = trackTags.select("span");
                for (Element track : tracks) {
                    String trackId = track.attr("id")
                       .replace("icon_download_", "");
                    String linkToApi = "https://www.bpmsupreme.com/store/output_file/" + trackId;
                    // TODO: 02.08.2019 API SERVICE TO GET FILE LINK
                    System.out.println(linkToApi);
                }
            }
            // System.out.println(element.text());
        }
    }
    
}
