package wordpress;

import json.TrackInfo;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Log
class TEST implements PosterInterface {
   @SneakyThrows
   public static void main(String[] args) {
      String nfoFilePath = "gfvid-geim6-480p.nfo";
      HashMap<Integer, TrackInfo> TrackList = new HashMap<>();
      String nfoSt = FileUtils.readFileToString(new File(nfoFilePath), "UTF-8")
        // .replaceAll("\\P{Print}", "")
        .trim();
      
      // String title = getFromNfo(nfoSt, "TITLE:", "TV DATE:");
      //
      // String Artist = title.split(" - ")[0];
      // String Genre = getFromNfo(nfoSt, "GENRE", "SUBGENRE");
      // String Released = getFromNfo(nfoSt, "SHOW DATE", "RUNTIME");
      // String Size = getFromNfo(nfoSt, "SIZE", "ARCHIVES");
      // String Playtime = getFromNfo(nfoSt, "RUNTIME", "GENRE");
      // String Format = getFromNfo(nfoSt, "CODEC", "BITRATE");
      // String Bitrate = getFromNfo(nfoSt, "AUDIO", "INFOS");
      // String Sample_Rate = getFromNfo(nfoSt, "INFOS", "FASHION");
      //
      // String trackList = getFromNfoSpace(nfoSt, "TRACKLIST", "NOTES");
      // if (trackList.replaceAll("\n", "").replaceAll(" ", "").equals("")) {
      //    TrackList.put(0, new TrackInfo(title.split(" - ")[1], Artist, Playtime));
      //
      // } else {
      //    String[] tracks = trackList.split("\n");
      //    for (int i = 0; i < tracks.length; i++) {
      //       String[] trackSplit = tracks[i].split(" {2}");
      //       TrackList.put(i, new TrackInfo(trackSplit[0], Artist, trackSplit[trackSplit.length - 1]));
      //    }
      // }
      // String Tracks = String.valueOf(TrackList.size());
      //
      // System.out.println(Artist);
      // System.out.println(Genre);
      // System.out.println(Released);
      // System.out.println(Size);
      // System.out.println(Playtime);
      // System.out.println(Format);
      // System.out.println(Bitrate);
      // System.out.println(Sample_Rate);
      // System.out.println(Tracks);
      // System.out.println(TrackList);
      // System.out.println(nfo);
   }
   
   @NotNull
   public static String getFromNfo(String nfo, String extract, String extractEnd) {
      return nfo.substring(nfo.indexOf(":", nfo.indexOf(extract)) + 1,
        nfo.indexOf(extractEnd)).trim();
   }
   
   public static String getFromNfoSpace(String nfo, String extract, String extractEnd) {
      return nfo.substring(nfo.indexOf("\n", nfo.indexOf(extract)),
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
   
   // @Override
   // @SuppressWarnings("Duplicates")
   // public String createCategory(String category, String parentId) throws ParseException {
   //    String apiURI = "https://myrecordpool.com/wp-json/wp/v2/categories";
   //    ResponseInfo responseInfo = postAndGetResponse(new Document()
   //        .append("name", category).append("parent", parentId).toJson(), apiURI,
   //      MRP_AUTHORIZATION);
   //    String categoryID = null;
   //    if (responseInfo.getCode() == 400) {
   //       categoryID = ((JSONObject) ((JSONObject) new JSONParser()
   //         .parse(responseInfo.getJsonBody())).get("data")).get("term_id").toString();
   //       System.out.println("Category found: " + category + " ID: " + categoryID);
   //
   //    } else if (responseInfo.getCode() == 201) {
   //       categoryID = new Document(Document.parse(responseInfo.getJsonBody()))
   //         .get("id").toString();
   //       System.out.println("Category created: " + category + " ID: " + categoryID);
   //    }
   //    return categoryID;
   // }
}
