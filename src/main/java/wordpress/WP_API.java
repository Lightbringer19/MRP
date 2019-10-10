package wordpress;

import json.InfoForPost;
import json.ResponseInfo;
import json.TrackInfo;
import json.WPPost;
import json.db.Task;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import utils.Constants;
import utils.FUtils;
import utils.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static collector.Collector.collect;
import static com.mongodb.client.model.Filters.eq;
import static wordpress.Poster.*;

public class WP_API {
   
   public static void main(String[] args) {
   }
   
   static void post(File jsonFile) {
      InfoForPost info = collect(jsonFile);
      String downloadID = DOWNLOAD_POSTER.addDownload(info.getReleaseName(), info.getLink());
      String htmlBodyForPost = buildHTML(info, downloadID);
      WPPost post = new WPPost(info.getReleaseName(), "publish",
        getCategoryIDsForPost(info), htmlBodyForPost);
      String linkToPost = createPostGetLinkToPost(post.toJson(), info.getReleaseName(),
        "https://myrecordpool.com/wp-json/wp/v2/posts",
        MRP_AUTHORIZATION);
      //update release in DB with Link
      Document release = MONGO_CONTROL.releasesCollection
        .find(eq("releaseName", info.getReleaseName())).first();
      release.put("mrpPostLink", linkToPost);
      ObjectId id = release.getObjectId("_id");
      MONGO_CONTROL.releasesCollection.replaceOne((eq("_id", id)), release);
      // add REPOST TASK to que
      Task task = new Task("repost", id.toString());
      MONGO_CONTROL.tasksCollection.insertOne(task.toDoc());
      Log.write("Posted: " + info.getReleaseName() + "| In: " + info.getPostCategory(),
        "Poster");
   }
   
   static String createPostGetLinkToPost(String JSON_BODY, String releaseName,
                                         String apiURI, String authorizationHeader) {
      try {
         while (true) {
            ResponseInfo response = postAndGetResponse(JSON_BODY, apiURI, authorizationHeader);
            if (response.getCode() != 201) {
               Log.write("Not posted: " + releaseName + " " + response.getCode(),
                 "Poster");
               Thread.sleep(7000);
               System.out.println(response.getJsonBody());
            } else {
               JSONObject post = (JSONObject) new JSONParser().parse(response.getJsonBody());
               return ((JSONObject) post.get("guid")).get("raw").toString();
            }
         }
      } catch (Exception e) {
         Log.write("EXCEPTION IN WP_API SENDER" + e, "Poster");
      }
      return null;
   }
   
   @SneakyThrows
   public static ResponseInfo postAndGetResponse(String JSON_BODY, String apiURI,
                                                 String authorizationHeader) {
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      HttpPost httpPost = new HttpPost(apiURI);
      httpPost.addHeader("Authorization", authorizationHeader);
      httpPost.addHeader("Content-Type", "application/json");
      httpPost.setEntity(new StringEntity(JSON_BODY, ContentType.APPLICATION_JSON));
      @Cleanup CloseableHttpResponse postResponse = client.execute(httpPost);
      String jsonResponse = EntityUtils.toString(postResponse.getEntity());
      return new ResponseInfo(postResponse.getStatusLine().getStatusCode(), jsonResponse);
   }
   
   private static String buildHTML(InfoForPost info, String downloadID) {
      String html_base = FUtils.readFile(new File(Constants.filesDir + "post.html"));
      
      html_base = html_base.replace("xartlinkx", info.getArtLink());
      html_base = html_base.replace("xreleasenamex", info.getReleaseName());
      html_base = html_base.replace("xartistx", info.getArtist());
      html_base = html_base.replace("xalbumx", info.getAlbum());
      html_base = html_base.replace("xgenrex", info.getGenre());
      html_base = html_base.replace("xreleasedx", info.getReleased());
      html_base = html_base.replace("xtracksx", info.getTracks());
      html_base = html_base.replace("xplaytimex", info.getPlaytime());
      html_base = html_base.replace("xgroupx", info.getGroup());
      html_base = html_base.replace("xformatx", info.getFormat());
      html_base = html_base.replace("xbitratex", info.getBitrate());
      html_base = html_base.replace("xsampleratex", info.getSample_Rate());
      html_base = html_base.replace("xsizex", info.getSize());
      
      html_base = html_base.replace("xlinkx", downloadID);
      
      HashMap<Integer, TrackInfo> TrackList = info.getTrackList();
      StringBuilder trackList = new StringBuilder();
      trackList.append("\r\n");
      for (HashMap.Entry<Integer, TrackInfo> track : TrackList.entrySet()) {
         int key = track.getKey() + 1;
         TrackInfo trackInfo = track.getValue();
         trackList.append("<tr>");
         trackList.append("\r\n");
         append(trackList, Integer.toString(key));
         append(trackList, trackInfo.getTitle());
         append(trackList, trackInfo.getArtist());
         append(trackList, trackInfo.getTime());
         trackList.append("</tr>");
      }
      html_base = html_base.replace("xtracklistx", trackList.toString());
      
      return html_base;
   }
   
   @SuppressWarnings("unchecked")
   private static Integer[] getCategoryIDsForPost(InfoForPost info) {
      String releaseName = info.getReleaseName();
      String category = info.getPostCategory();
      Map<String, String> categoriesAndIDs;
      List<Integer> categories = new ArrayList<>();
      switch (category) {
         case "BEATPORT":
            categories.add(Integer.valueOf("115"));
            setCategoriesForBeatAndScene(info, categories);
            break;
         case "RECORDPOOL MUSIC":
            categories.add(Integer.valueOf("77"));
            setCategories(releaseName, categories, category);
            break;
         case "RECORDPOOL VIDEOS":
            categories.add(Integer.valueOf("29"));
            setCategories(releaseName, categories, category);
            break;
         case "SCENE-MP3":
            categories.add(Integer.valueOf("184"));
            setCategoriesForBeatAndScene(info, categories);
            break;
         case "SCENE-FLAC":
            categories.add(Integer.valueOf("191"));
            setCategoriesForBeatAndScene(info, categories);
            break;
      }
      return categories.toArray(new Integer[0]);
   }
   
   private static void setCategoriesForBeatAndScene(InfoForPost info, List<Integer> categories) {
      if (!info.getGenre().equals("Mixed")) {
         String genreFiltered = info.getGenre()
           .replaceAll("\\)", "").replaceAll("\\(", "")
           .replaceAll("^[0-9]", "").trim();
         Integer genre = createCategory(genreFiltered, "5514");
         categories.add(genre);
      }
      String artistInfo = info.getArtist().trim();
      if (!artistInfo.equals("") && !artistInfo.equals(" ")) {
         Integer artist = createCategory(artistInfo, "5513");
         categories.add(artist);
      }
   }
   
   @SuppressWarnings("unchecked")
   private static void setCategories(String releaseName, List<Integer> categories, String category) {
      Map<String, String> categoriesAndIDs;
      categoriesAndIDs = (Map<String, String>)
        MONGO_CONTROL.categoriesCollection
          .find(eq("name", category))
          .first().get("categoriesAndIDs");
      for (Map.Entry<String, String> categoryAndID : categoriesAndIDs.entrySet()) {
         if (releaseName.contains(categoryAndID.getKey())) {
            categories.add(Integer.valueOf(categoryAndID.getValue()));
            break;
         }
      }
   }
   
   @SuppressWarnings("Duplicates")
   @SneakyThrows
   public static Integer createCategory(String category, String parentId) {
      String apiURI = "https://myrecordpool.com/wp-json/wp/v2/categories";
      String categoryID;
      while (true) {
         ResponseInfo responseInfo = postAndGetResponse(new Document()
             .append("name", category).append("parent", parentId).toJson(), apiURI,
           MRP_AUTHORIZATION);
         if (responseInfo.getCode() == 400) {
            categoryID = ((JSONObject) ((JSONObject) new JSONParser()
              .parse(responseInfo.getJsonBody())).get("data")).get("term_id").toString();
            System.out.println("Category found  : " + category + " ID: " + categoryID);
            return Integer.valueOf(categoryID);
            
         } else if (responseInfo.getCode() == 201) {
            categoryID = new Document(Document.parse(responseInfo.getJsonBody()))
              .get("id").toString();
            System.out.println("Category created: " + category + " ID: " + categoryID);
            return Integer.valueOf(categoryID);
         } else {
            Log.write("Error on search: " + category +
                " RESPONSE CODE: " + responseInfo.getCode(),
              "Poster");
            Log.write(responseInfo.getJsonBody(), "Poster");
            Thread.sleep(3000);
         }
      }
   }
   
   private static void append(StringBuilder trackList, String valueString) {
      trackList.append("<td>");
      trackList.append(valueString);
      trackList.append("</td>");
      trackList.append("\r\n");
   }
}
