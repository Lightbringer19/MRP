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
import poster.PosterInterface;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Log
class TEST implements PosterInterface {
   @SneakyThrows
   public static void main(String[] args) {
      String nfoFilePath = "GDS_SERVER/clara_luciani-la_grenade_(2019_nrj_music_awards)-fr-720p-x264-2019-pmv.nfo";
      HashMap<Integer, TrackInfo> TrackList = new HashMap<>();
      String nfoSt = FileUtils.readFileToString(new File(nfoFilePath), "UTF-8")
        .replaceAll("\\P{Print}", "")
        .trim();
      
      String Artist = getFromNfo(nfoSt, "ARTiST", "TiTLE");
      String title = getFromNfo(nfoSt, "TiTLE", "REL.DATE")
        .replaceAll("~", "").trim();
      String Genre = getFromNfo(nfoSt, "GENRE", "FORMAT")
        .replaceAll("~", "").trim();
      String Released = getFromNfo(nfoSt, "REL.DATE", "AIR DATE");
      String Size = getFromNfo(nfoSt, "SIZE", "Bytes");
      long fileSizeInKB = Long.parseLong(Size) / 1024;
      long fileSizeInMB = fileSizeInKB / 1024;
      Size = fileSizeInMB + " MB";
      String Playtime = getFromNfo(nfoSt, "LENGTH", "DAR/SAR");
      String Format = getFromNfo(nfoSt, "VIDEO CODEC", "AUDIO CODEC");
      String Bitrate = getFromNfo(nfoSt, "AVERAGE BITRATE:", "CONTAINER");
      
      // String Sample_Rate = getFromNfoDot(nfoSt, "INFOS", "FASHION");
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
      
      TrackList.put(0, new TrackInfo(title, Artist, Playtime));
      
      System.out.println(title);
      System.out.println(Artist);
      System.out.println(Genre);
      System.out.println(Released);
      System.out.println(Size);
      System.out.println(Playtime);
      System.out.println(Format);
      System.out.println(Bitrate);
      // System.out.println(Sample_Rate);
      // System.out.println(Tracks);
      System.out.println(TrackList);
      // System.out.println(nfoSt);
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
   
   @NotNull
   public static String getFromNfoDot(String nfo, String extract, String extractEnd) {
      return nfo.substring(nfo.indexOf(" ", nfo.indexOf(".", nfo.indexOf(extract)) + 1),
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
