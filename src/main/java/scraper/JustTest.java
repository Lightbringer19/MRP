package scraper;

public class JustTest {
    public static void main(String[] args) {
        String currentUrl = "https://www.bpmsupreme.com/store/newreleases/audio/classic/11";
        Integer pageNumber =
           Integer.valueOf(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        String finalUrl = currentUrl.replace(pageNumber.toString(), String.valueOf(pageNumber + 1));
        System.out.println(finalUrl);
    }
}
