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
        Elements trackInfos = document.select("li[class=even updatedversion ng-scope]");
        for (Element trackInfo : trackInfos) {
            String date = trackInfo.select("span[class=date ng-binding]").text();
            if (date.equals(downloadDate)) {
                String title = trackInfo.select("div[class=title_box]>h3").first()
                   .attr("title");
                Element trackTags = trackInfo.select("div[class=tag]").first();
                Elements trackDownloadInfos = trackTags.select("span");
                for (Element downloadInfo : trackDownloadInfos) {
                    String trackId = downloadInfo.attr("id")
                       .replace("icon_download_", "");
                    String trackType = downloadInfo.text();
                    // String linkToApi = "https://www.bpmsupreme.com/store/output_file/" + trackId;
                    // String downloadUrl = getDownloadUrl(linkToApi);
                    System.out.println(title + " " + trackType + " " + trackId);
                    // scrapedLinks.add(downloadUrl);
                    // System.out.println(linkToApi);
                }
            }
        }
    }
    
}
