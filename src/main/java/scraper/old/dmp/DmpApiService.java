package scraper.old.dmp;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Log;

import java.io.IOException;
import java.text.MessageFormat;

import static scraper.old.dmp.DmpApiService.Extractor.*;
import static wordpress.ApiInterface.getClean;
import static wordpress.ApiInterface.postClean;

class DmpApiService {
   
   static String getDownloadUrl(String scraped, String cookie)
     throws IOException, ParseException {
      String uri = "https://www.digitalmusicpool.com" + scraped;
      String response = getClean(uri, cookie);
      // Log.write(response, "DMP Scraper");
      String TrackID = scraped.substring(scraped.lastIndexOf("/") + 1);
      String downloadKey = null;
      if (response.contains("Beginning Download")) {
         downloadKey = getDownloadKey(response, TrackID);
      } else if (response.contains("You have exceeded the download limit for this track")) {
         Log.write("Limit, Skip Track", "DMP Scraper");
      } else {
         Request requestInfo = getRequestInfo(response, scraped);
         String postResponse = postClean(uri, cookie, requestInfo.toString());
         // Log.write(postResponse, "DMP Scraper");
         JSONObject post = (JSONObject) new JSONParser().parse(postResponse);
         downloadKey = post.get("DownloadFileKey").toString();
      }
      //final POST
      if (downloadKey == null) {
         return null;
      } else {
         String bodyTemp = "TrackType=originals&TrackID={0}&DownloadKey={1}";
         String body = MessageFormat.format(bodyTemp, TrackID, downloadKey);
         String post = postClean("https://www.digitalmusicpool.com/download", cookie, body);
         String url = ((JSONObject) new JSONParser().parse(post)).get("url").toString();
         Log.write("Got Url: " + url, "DMP Scraper");
         return url;
      }
      
   }
   
   static class Extractor {
      
      static String getDownloadKey(String response2, String trackID) {
         int index1 = response2.indexOf("\"", response2.indexOf(trackID));
         int index2 = response2.indexOf("\"", index1 + 2);
         return response2.substring(index1 + 1, index2);
      }
      
      static Request getRequestInfo(String response, String scraped) {
         if (response.contains("RH:")) {
            
            int indexOf = response.indexOf("RH: \"");
            int lastIndexOf = response.indexOf("\"", indexOf + 5);
            String RH = response.substring(indexOf + 5, lastIndexOf);
            
            int lastIndexOfToken = response.lastIndexOf("value=\"");
            int latsToken = response.indexOf("\"", lastIndexOfToken + 8);
            String __RequestVerificationToken =
              response.substring(lastIndexOfToken + 7, latsToken);
            
            String TrackID = scraped.substring(scraped.lastIndexOf("/") + 1);
            String TrackTypeID = "originals";
            
            Request request = new Request();
            request.set__RequestVerificationToken(__RequestVerificationToken);
            request.setRH(RH);
            request.setTrackID(TrackID);
            request.setTrackTypeID(TrackTypeID);
            
            System.out.println(request.toString());
            return request;
         }
         return null;
      }
      
      @Data
      @NoArgsConstructor
      static class Request {
         String __RequestVerificationToken;
         String RecaptchaResponse = "";
         String VoteValue = "4";
         String VoteComment = "";
         String RH;
         String TrackTypeID;
         String TrackID;
         
         @Override
         public String toString() {
            return
              "__RequestVerificationToken=" + __RequestVerificationToken +
                "&RecaptchaResponse=" + RecaptchaResponse +
                "&VoteValue=" + VoteValue +
                "&VoteComment=" + VoteComment +
                "&RH=" + RH +
                "&TrackTypeID=" + TrackTypeID +
                "&TrackID=" + TrackID;
         }
         
      }
   }
}
