package scraper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AnotherTest {
   public static void main(String[] args) throws ParseException {
      String dateFormat = "MMMM";
      
      String scrapedDate = "January Videos".replace(" Videos", "");
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.set(Calendar.DAY_OF_MONTH, 1);
      cal.set(Calendar.MONTH, new SimpleDateFormat(dateFormat, Locale.US)
        .parse(scrapedDate).getMonth());
      cal.add(Calendar.MONTH, -1);
      String format = new SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.getTime());
      System.out.println(format);
      
   }
}
