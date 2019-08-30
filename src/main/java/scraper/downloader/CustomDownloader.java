package scraper.downloader;

import mongodb.MongoControl;
import org.bson.Document;
import scraper.abstraction.DownloadInterface;
import utils.Logger;

import java.util.List;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;

public class CustomDownloader implements DownloadInterface {
    
    private static final Scanner IN = new Scanner(System.in);
    private Logger logger = new Logger("Custom Downloader");
    private static String COOKIE;
    
    public static void main(String[] args) {
        MongoControl mongoControl = new MongoControl();
        CustomDownloader customDownloader = new CustomDownloader();
        System.out.println("Enter release name: ");
        String releaseName = IN.nextLine();
        System.out.println("Enter cookies: ");
        COOKIE = IN.nextLine();
        Document releaseInfo = mongoControl.scrapedReleases
           .find(eq("releaseName", releaseName)).first();
        List<String> scrapedLinks = (List<String>) releaseInfo.get("scrapedLinks");
        customDownloader.downloadLinks(scrapedLinks, releaseName);
    }
    
    @Override
    public String getCookie() {
        return COOKIE;
    }
    
    @Override
    public Logger getLogger() {
        return logger;
    }
}
