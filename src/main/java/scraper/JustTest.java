package scraper;

import java.io.IOException;

public class JustTest {
    public static void main(String[] args) throws IOException {
        String currentUrl = "https://dalemasbajo.com/20";
        String pageNumber = currentUrl.substring(currentUrl.lastIndexOf("/") + 1);
        String newLink = "https://dalemasbajo.com/" +
           (Integer.parseInt(pageNumber) + 20);
        System.out.println(newLink);
    }
}
