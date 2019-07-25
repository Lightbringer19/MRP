package scraper.dmp;

import lombok.AllArgsConstructor;
import lombok.Data;
import mongodb.MongoControl;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.FUtils;
import utils.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static scraper.dmp.DmpApiService.getDownloadUrl;
import static scraper.dmp.DmpDriver.cookieForAPI;

class DmpScraper {
    
    private static MongoControl mongoControl = new MongoControl();
    private static String date;
    
    static void checkForNewRelease(String html)
       throws IOException, ParseException, java.text.ParseException {
        ScrapedDmpPage scrapedDmpPage = scrapePageForDatesAndLinks(html);
        List<String> dates = scrapedDmpPage.getDates();
        String newDate = dates.get(0);
        boolean newReleaseNotInDB = mongoControl.scrapedDMP
           .find(eq("releaseDate", newDate)).first() == null;
        if (newReleaseNotInDB) {
            Log.write("Found New Release: " + newDate, "Scraper");
            mongoControl.scrapedDMP.insertOne(new org.bson.Document("releaseDate", newDate));
            String dateForDownloadRaw = dates.get(1);
            Collection<String> linksToDownload = scrapedDmpPage
               .getScraped().get(dateForDownloadRaw);
            Log.write("Scraping Previous Release: " + dateForDownloadRaw, "Scraper");
            // GET URLS VIA API
            List<String> downloadURLS = getDownloadUrls(linksToDownload);
            // DOWNLOAD RELEASE FROM PREVIOUS DATE
            addToDownloadQueue(getDateForDmpDownload(dateForDownloadRaw), downloadURLS);
        }
    }
    
    private static ScrapedDmpPage scrapePageForDatesAndLinks(String html) {
        Document document = Jsoup.parse(html);
        MultiValuedMap<String, String> scraped = new HashSetValuedHashMap<>();
        Element table = document.select("table[id=SongTableData]").first();
        Elements trElements = table.select("tr");
        List<String> dates = new ArrayList<>();
        for (Element track : trElements) {
            if (track.select("td[class=SongTableDataRow]").hasText()) {
                date = track.text();
                dates.add(date);
            }
            if (track.className().equals("SongTableOdd") || track.className().equals("SongTableEven")) {
                String trackName = track.text();
                Elements links = track.select("td[class=ctr]");
                for (Element img : links) {
                    String imgHtml = img.html();
                    if (imgHtml.contains("openDetermineDownloadDialog")) {
                        int index = imgHtml.indexOf("'");
                        int lastIndex = imgHtml.lastIndexOf("'");
                        String scrapedInfoForDownload = imgHtml.substring(index + 1, lastIndex);
                        scraped.put(date, scrapedInfoForDownload);
                    }
                }
            }
        }
        return new ScrapedDmpPage(scraped, dates);
    }
    
    private static List<String> getDownloadUrls(Collection<String> links)
       throws IOException, ParseException {
        List<String> downloadURLS = new ArrayList<>();
        for (String link : links) {
            Log.write("Will Scrape link from: " + link, "Scraper");
            String downloadURL = getDownloadUrl(link, cookieForAPI);
            if (downloadURL != null) {
                downloadURLS.add(downloadURL);
            }
        }
        return downloadURLS;
    }
    
    private static void addToDownloadQueue(String dateForReleaseName, List<String> downloadURLS) {
        String scrapedLinks = String.join(" ", downloadURLS)
           .replaceAll("\"", "");
        String releaseName = "Digital Music Pool " + dateForReleaseName;
        Log.write("All Scraped Links: " + scrapedLinks, "Scraper");
        Log.write("Adding Release to QUEUE: " + releaseName, "Scraper");
        FUtils.writeToFile(releaseName, scrapedLinks);
        mongoControl.toDownloadCollection.insertOne(
           new org.bson.Document("releaseName", releaseName)
              .append("link", scrapedLinks).append("categoryName", "RECORDPOOL"));
    }
    
    private static String getDateForDmpDownload(String date) throws java.text.ParseException {
        SimpleDateFormat DATE_FORMAT =
           new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.setTime(DATE_FORMAT.parse(date));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        System.out.println(DATE_FORMAT.format(cal.getTime()));
        return new SimpleDateFormat("ddMM").format(cal.getTime());
    }
    
    @Data
    @AllArgsConstructor
    static class ScrapedDmpPage {
        MultiValuedMap<String, String> scraped;
        List<String> dates;
    }
    
}
