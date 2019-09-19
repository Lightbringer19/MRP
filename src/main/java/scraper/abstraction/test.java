package scraper.abstraction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class test {
   
   public static void main(String[] args) throws ParseException {
      SimpleDateFormat formatter = new SimpleDateFormat("MM-MMM", Locale.US);
      String month = formatter
        .format(new Date()).toUpperCase();
      String pathname = "/AUDIO/DATES/2019/" + month + "/";
      System.out.println(pathname);
      
      Calendar cal = Calendar.getInstance();
      // cal.setTime(formatter.parse(month));
      cal.add(Calendar.MONTH, -1);
      
      String previousMonth = formatter
        .format(cal.getTime()).toUpperCase();
      pathname = "/AUDIO/DATES/2019/" + previousMonth + "/";
      System.out.println(pathname);
   }
   
}
