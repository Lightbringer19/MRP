package monitors;

import scraper.latin_video_remix.LatinVideoRScraper;

public class LatinVideoRemix_Scraper_Monitor extends Monitor {
   
   public static void main(String[] args) {
      setTITLE("Latin Video Remix Scraper");
      doAll(new LatinVideoRScraper());
   }
}
