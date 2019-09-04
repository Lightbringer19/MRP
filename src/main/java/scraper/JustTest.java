package scraper;

import scraper.avdistrict.ApiServiceAvDistrict;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JustTest implements ApiServiceAvDistrict {
   public static void main(String[] args) throws IOException, ParseException {
      
      // JustTest justTest = new JustTest();
      // String downloadLink = justTest.getDownloadLink("27143");
      // System.out.println(downloadLink);
   }
   
   public static boolean releaseIsOld(String dateFormat, String date) throws ParseException {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new SimpleDateFormat(dateFormat, Locale.US).parse(date));
      cal.add(Calendar.DAY_OF_MONTH, 2);
      long nowTime = new Date().getTime();
      return nowTime > cal.getTime().getTime();
   }
   
   public static void nextPage() {
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
   
   // @Override
   // public Logger getLogger() {
   //    return new Logger("TEST");
   // }
}
