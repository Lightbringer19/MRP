package wordpress;

import json.ResponseInfo;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static wordpress.Poster.MRP_AUTHORIZATION;
import static wordpress.WP_API.postAndGetResponse;

@Log
class TEST {
   @SneakyThrows
   public static void main(String[] args) {
      String nfoFilePath = "bazzi-honest-ddc-1080p-x264-2018-srpx.nfo";
      String nfo = FileUtils.readFileToString(new File(nfoFilePath), "UTF-8")
        .replaceAll("\\P{Print}", "")
        .trim();
      String artist = getFromNfo(nfo, "Artist", "Track Title");
      String title = getFromNfo(nfo, "Title", "Genre");
      String genre = getFromNfo(nfo, "Genre", "Year");
      String year = getFromNfo(nfo, "Year", "Rip date");
      String length = getFromNfo(nfo, "Length", "Size");
      String format = getFromNfo(nfo, "Format", "Audio");
      String audio = getFromNfo(nfo, "Audio", "Deinterlace");
      
      String sampleRate = Arrays.stream(audio.split(" "))
        .filter(s -> s.contains("Hz"))
        .findFirst()
        .orElse("???Hz");
      String bitrate = Arrays.stream(audio.split(" "))
        .filter(s -> s.contains("kbps"))
        .findFirst()
        .orElse("???kbps");
      
      System.out.println(artist);
      System.out.println(title);
      System.out.println(genre);
      System.out.println(year);
      System.out.println(length);
      System.out.println(format);
      // System.out.println(audio);
      System.out.println(sampleRate);
      System.out.println(bitrate);
      
      // System.out.println(nfo);
   }
   
   @NotNull
   public static String getFromNfo(String nfo, String extract, String extractEnd) {
      return nfo.substring(nfo.indexOf(" ", nfo.indexOf(extract)),
        nfo.indexOf(extractEnd)).trim();
   }
   
   public static void a() throws IOException {
      String url = "https://myrecordpool.com/wp-json/wp/v2/posts/" + 20000;
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      HttpGet get = new HttpGet(url);
      @Cleanup CloseableHttpResponse response = client.execute(get);
      int responseCode = response.getStatusLine().getStatusCode();
      System.out.println(responseCode);
      System.out.println(EntityUtils.toString(response.getEntity()));
   }
   
   @SuppressWarnings("Duplicates")
   public static String createCategory(String category, String parentId) throws ParseException {
      String apiURI = "https://myrecordpool.com/wp-json/wp/v2/categories";
      ResponseInfo responseInfo = postAndGetResponse(new Document()
          .append("name", category).append("parent", parentId).toJson(), apiURI,
        MRP_AUTHORIZATION);
      String categoryID = null;
      if (responseInfo.getCode() == 400) {
         categoryID = ((JSONObject) ((JSONObject) new JSONParser()
           .parse(responseInfo.getJsonBody())).get("data")).get("term_id").toString();
         System.out.println("Category found: " + category + " ID: " + categoryID);
         
      } else if (responseInfo.getCode() == 201) {
         categoryID = new Document(Document.parse(responseInfo.getJsonBody()))
           .get("id").toString();
         System.out.println("Category created: " + category + " ID: " + categoryID);
      }
      return categoryID;
   }
}
