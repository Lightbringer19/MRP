package scraper;

import scraper.abstraction.DownloadInterface;
import utils.Logger;

import java.io.IOException;

public class JustTest implements DownloadInterface {
    public static void main(String[] args) throws IOException {
        // JustTest justTest = new JustTest();
        // justTest.downloadFile("", "Z:\\TEMP FOR LATER\\");
        String currentUrl = "https://maletadvj.com/audios/40";
        String pageNumber = currentUrl.substring(currentUrl.lastIndexOf("/") + 1);
        String newLink;
        if (!pageNumber.matches("^[0-9]+$")) {
            newLink = currentUrl + "/" + 20;
        } else {
            newLink = currentUrl.replace(pageNumber,
               String.valueOf(Integer.parseInt(pageNumber) + 20));
        }
        System.out.println(newLink);
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
