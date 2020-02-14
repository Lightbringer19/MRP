package scraper.abstraction;

import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public interface ScrapingInterface {
   
   default String scrapeFirstDate(String html) {
      return null;
   }
   
   default String previousDateOnThisPage(String html, String firstDate) {
      return null;
   }
   
   default void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
   }
   
   default void nextPage() {
   }
   
   default List<String> scrapePlaylist(String playListUrl) {
      return null;
      
   }
   
   @SneakyThrows
   default String formatDownloadDate(String date, String dateFormat) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new SimpleDateFormat(dateFormat, Locale.US).parse(date));
      cal.add(Calendar.DAY_OF_MONTH, 1);
      return new SimpleDateFormat("ddMM").format(cal.getTime());
   }
}
