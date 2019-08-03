package scraper.bpm;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import scraper.abstraction.Scraper;

import java.util.List;

public class BpmSupreme extends Scraper implements ApiService {
    
    public static void main(String[] args) {
        BpmSupreme bpmSupreme = new BpmSupreme();
        bpmSupreme.run();
    }
    
    public BpmSupreme() {
        dateFormat = "MM/dd/yy";
        downloaded = mongoControl.bpmDownloaded;
        releaseName = "Bpm Supreme";
    
        loginAtFirstStage = false;
        urlForFirstStage = "https://www.bpmsupreme.com/store/newreleases/audio/classic/1";
    }
    
    @Override
    @SneakyThrows
    public void afterFirstStage() {
        Thread.sleep(10_000);
    }
    
    @Override
    public String scrapeFirstDate(String html) {
        return Jsoup.parse(html).select("span[class=date ng-binding]").first().text();
    }
    
    @Override
    public String previousDateOnThisPage(String html, String firstDate) {
        return Jsoup.parse(html)
           .select("span[class=date ng-binding]")
           .stream()
           .filter(date -> !date.text().equals(firstDate))
           .findFirst()
           .map(Element::text)
           .orElse(null);
    }
    
    @Override
    public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate,
                                     List<String> scrapedLinks) {
        Elements trackInfos = Jsoup.parse(html)
           .select("li[class=even updatedversion ng-scope]");
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
                    String linkForApi = "https://www.bpmsupreme.com/store/output_file/" + trackId;
                    String downloadUrl = getDownloadUrl(linkForApi);
                    String trackType = downloadInfo.text();
                    logger.log(title + " (" + trackType + ") | "
                       + downloadUrl
                    );
                    scrapedLinks.add(downloadUrl);
                }
            }
        }
    }
    
    @Override
    @SneakyThrows
    public void nextPage() {
        String currentUrl = driver.getCurrentUrl();
        Integer pageNumber =
           Integer.valueOf(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        String finalUrl = currentUrl.replace(pageNumber.toString(), String.valueOf(pageNumber + 1));
        driver.get(finalUrl);
        Thread.sleep(10_000);
    }
}
