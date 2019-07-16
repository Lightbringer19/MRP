package scraper.eighth_wonder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

import static scraper.eighth_wonder.EwDriver.ewLogger;

class EwScraper {

    public static void main(String[] args) {
    }

    List<String> scrapeAllLinksOnPage(String html, String date, List<String> scrapedLinks) {
        Document document = Jsoup.parse(html);
        Elements tracks = document.select("div[class=tracks-list__item]");
        for (Element track : tracks) {
            Element trackInfo = track.select("div[class=col-sm-4 m-w-100]").first();
            String releaseDate = trackInfo.select("div[class=col-sm-12 m-mar-l-20p " +
                    "no-padding]>div[class=col-sm-2 no-padding]").first().text();
            if (releaseDate.equals(date)) {
                String title = trackInfo.select("h4[class=cursor-pointer]")
                        .first().attr("title");
                String downloadUrl = track.select("a[class=btn-download tracks-list__" +
                        "action-button download-btn]").attr("href");
                // WRITE TO DB
                ewLogger.log("Adding Track to Download List: "
                        + title + " | " + releaseDate);
                scrapedLinks.add(downloadUrl);
                // mongoControl.ewTracks.insertOne(ewTrackInfo.toDoc());
            }
        }
        return scrapedLinks;
    }

    String previousDateOnThisPage(String html, String date) {
        Document document = Jsoup.parse(html);
        Elements tracks = document.select("div[class=tracks-list__item]");
        for (Element track : tracks) {
            Element trackInfo = track.select("div[class=col-sm-4 m-w-100]").first();
            String releaseDate = trackInfo.select("div[class=col-sm-12 m-mar-l-20p " +
                    "no-padding]>div[class=col-sm-2 no-padding]").first().text();
            if (!releaseDate.equals(date)) {
                return releaseDate;
            }
        }
        return null;
    }

    String scrapeDate(String html) {
        Document document = Jsoup.parse(html);
        Element firstTrack = document.select("div[class=tracks-list__item]").first();
        Element trackInfo = firstTrack.select("div[class=col-sm-4 m-w-100]").first();
        return trackInfo.select("div[class=col-sm-12 m-mar-l-20p " +
                "no-padding]>div[class=col-sm-2 no-padding]").first().text();
    }
}
