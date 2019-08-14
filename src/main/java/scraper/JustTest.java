package scraper;

public class JustTest {
    public static void main(String[] args) {
        
        String currentUrl = "https://www.remixmp4.com/index.php?action=&name=&artist=&price1=&price2=&order=&genre=&page=1";
        int pageNumber =
           Integer.parseInt(currentUrl.substring(currentUrl.lastIndexOf("=") + 1));
        String newLink = "https://www.remixmp4.com/index" +
           ".php?action=&name=&artist=&price1=&price2=&order=&genre=&page=" + (pageNumber + 1);
        System.out.println(newLink);
        // driver.get(newLink);
        
    }
}
