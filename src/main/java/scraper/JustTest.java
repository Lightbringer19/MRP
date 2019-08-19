package scraper;

import scraper.abstraction.DownloadInterface;
import utils.Logger;

import java.io.IOException;

public class JustTest implements DownloadInterface {
    public static void main(String[] args) throws IOException {
        JustTest justTest = new JustTest();
        justTest.downloadFile("", "Z:\\TEMP FOR LATER\\");
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
