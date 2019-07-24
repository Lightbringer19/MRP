package scraper.mp3pool;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

import static scraper.mp3pool.Mp3poolDriver.mp3Logger;

class Mp3PoolScraper {

    Mp3PoolScraper() {
    }

    String scrapeDate(String html) {
        Document document = Jsoup.parse(html);
        Element release = document.select("div[class=innerPlayer1]").first();
        return release.select("p").first().text()
                .replace("Added On: ", "");
    }

    String previousDateOnThisPage(String html, String date) {
        Document document = Jsoup.parse(html);
        Elements releases = document.select("div[class=innerPlayer1]");
        return releases.stream()
                .map(release -> release.select("p").first().text()
                        .replace("Added On: ", ""))
                .filter(releaseDate -> !releaseDate.equals(date))
                .findFirst()
                .orElse(null);
    }

    List<String> scrapeAllLinksOnPage(String html, String date, List<String> scrapedLinks) {
        Document document = Jsoup.parse(html);
        Elements releases = document.select("div[class=innerPlayer1]");
        for (Element release : releases) {
            String releaseDate = release.select("p").first().text()
                    .replace("Added On: ", "");
            if (releaseDate.equals(date)) {
                Elements tracks = release.select("div>ul>li");
                tracks.forEach(track -> {
                    String title = track.select("div[class=track-title]").text();
                    Elements links = track.select("div[class=download2 sub-section]>a");
                    if (!title.equals("")) {
                        mp3Logger.log("Adding Track to Download List: "
                                + title + " | " + releaseDate);
                    }
                    links.stream()
                            .map(link -> link.attr("href"))
                            .filter(downloadUrl -> downloadUrl.contains("download/"))
                            .forEach(scrapedLinks::add);
                });
            }
        }
        return scrapedLinks;
    }
}
