package scraper;

import scraper.abstraction.DownloadInterface;
import scraper.bpm.ApiService;
import utils.Logger;

import java.util.Collections;

public class DownloaderTest implements DownloadInterface, ApiService {
    public static void main(String[] args) {
        // String[] links = {
        // };
        String url = "https://www.bpmsupreme.com/store/output_file/329908";
        DownloaderTest downloaderTest = new DownloaderTest();
        String downloadUrl = downloaderTest.getDownloadUrl(url);
        downloaderTest.downloadLinks(Collections.singletonList(downloadUrl),
           "TESTDOWNLOAD");
        System.out.println(downloadUrl);
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
