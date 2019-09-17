package scraper;

import org.bson.Document;
import scraper.avdistrict.AvDistrictApiService;
import utils.FUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JustTest implements AvDistrictApiService {
   public static void main(String[] args) throws IOException, ParseException {
      // String response = getClean("http://www.clubdjvideos.com/videos/set/first?ascending=false&totalSize=1000&index=1&sortField=dateAdded&viewableSize=300",
      //   "");
      // System.out.println(response);
      String response = FUtils.readFile(new File("Z:\\source.html"));
      Document user = (Document) Document.parse(response).get("user");
      System.out.println("CREDITS LEFT: " + user.get("downloadCredit").toString());
      // ClubDJScraper.FullInfo info = new Gson().fromJson(response, ClubDJScraper.FullInfo.class);
      // List<ClubDJScraper.FullInfo.VideosBean> videos = info.getVideos();
      //
      // String firstDate = new SimpleDateFormat("dd-MM-yyyy").format(videos.get(0).getDateAdded());
      //
      // System.out.println(firstDate);
      //
      // String downloadDate = videos
      //   .stream()
      //   .map(video -> new SimpleDateFormat("dd-MM-yyyy").format(video.getDateAdded()))
      //   .filter(date -> !date.equals(firstDate))
      //   .findFirst()
      //   .orElse(null);
      //
      // System.out.println(downloadDate);
      // for (ClubDJScraper.FullInfo.VideosBean video : videos) {
      //    String trackDate = new SimpleDateFormat("dd-MM-yyyy")
      //      .format(video.getDateAdded());
      //    if (trackDate.equals(downloadDate)) {
      //       String requestUrl =
      //         "http://www.clubdjvideos.com/download-info?videoId=" + video.getId();
      //       String purchaseInfo = getClean(requestUrl, cookie);
      //       String downloadLink = (String) Document.parse(purchaseInfo).get("downloadLink");
      //       String trackName = video.getTitle();
      //       System.out.println(downloadLink + " " + trackName);
      //       System.out.println(trackName);
      //    }
      // }
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
