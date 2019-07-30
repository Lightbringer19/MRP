package scraper;

import scraper.abstraction.DownloadInterface;
import utils.Logger;

import java.util.Arrays;

public class DownloaderTest implements DownloadInterface {
    public static void main(String[] args) {
        String[] links = {
        };
        new DownloaderTest().downloadLinks(Arrays.asList(links), "TESTDOWNLOAD");
    }
    
    @Override
    public String getCookie() {
        return "";
    }
    
    @Override
    public Logger getLogger() {
        return new Logger("TEST");
    }
}
