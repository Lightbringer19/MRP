package scraper;

public class JustTest {
    public static void main(String[] args) {
        
        String link = "https://av.bpmsupreme.com/video/Alessia Cara - Here (Vincent Remix) (Clean Short Edit).mp4?download";
        String replaced = link.replaceAll(" ", "%20");
        System.out.println(replaced);
    }
}
