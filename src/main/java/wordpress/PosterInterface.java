package wordpress;

import com.google.gson.Gson;
import json.ResponseInfo;
import json.WPPost;
import json.db.InfoAboutRelease;
import json.db.InfoAboutRelease.Track;
import json.db.Release;
import json.db.Task;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static wordpress.Poster.MONGO_CONTROL;
import static wordpress.Poster.MRP_AUTHORIZATION;

public interface PosterInterface {
   
   default void post(String releaseName) {
      Document releaseDoc = MONGO_CONTROL.releasesCollection
        .find(eq("releaseName", releaseName)).first();
      ObjectId id = releaseDoc.getObjectId("_id");
      Release release = new Gson().fromJson(releaseDoc.toJson(), Release.class);
      String htmlBodyForPost = buildHTML(release);
      WPPost post = new WPPost(release.getReleaseName(), "publish",
        getCategoryIDsForPost(release), htmlBodyForPost);
      String linkToPost = createPostGetLinkToPost(post.toJson(), release.getReleaseName(),
        "https://myrecordpool.com/wp-json/wp/v2/posts",
        MRP_AUTHORIZATION);
      //update release in DB with Link
      releaseDoc.put("mrpPostLink", linkToPost);
      MONGO_CONTROL.releasesCollection.replaceOne((eq("_id", id)), releaseDoc);
      // add REPOST TASK to que
      Task task = new Task("repost", id.toString());
      MONGO_CONTROL.tasksCollection.insertOne(task.toDoc());
      Log.write("Posted: " + release.getReleaseName() + "| In: " + release.getCategory(),
        "Poster");
      //for back up
      try {
         FileUtils.writeStringToFile(
           new File(release.getPathToLocalFolder().replace("E:", "C:")
             + "\\myrecordpool.com.txt"),
           release.getBoxComDownloadLink(), "UTF-8");
      } catch (IOException e) {
         Log.write(e, "Poster");
      }
   }
   
   default String createPostGetLinkToPost(String JSON_BODY, String releaseName,
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
   default ResponseInfo postAndGetResponse(String JSON_BODY, String apiURI,
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
   
   default String buildHTML(Release info) {
      String html_base = FUtils.readFile(new File(Constants.filesDir + "post.html"));
      
      InfoAboutRelease infoAboutRelease = info.getInfoAboutRelease();
      html_base = html_base.replace("xartlinkx", infoAboutRelease.getLinkToArt());
      html_base = html_base.replace("xreleasenamex", info.getReleaseName());
      html_base = html_base.replace("xartistx", infoAboutRelease.getArtist());
      html_base = html_base.replace("xalbumx", infoAboutRelease.getAlbum());
      html_base = html_base.replace("xgenrex", infoAboutRelease.getGenre());
      html_base = html_base.replace("xreleasedx", infoAboutRelease.getReleased());
      html_base = html_base.replace("xtracksx", infoAboutRelease.getNumberOfTracks());
      html_base = html_base.replace("xplaytimex", infoAboutRelease.getPlaytime());
      html_base = html_base.replace("xgroupx", infoAboutRelease.getGroup());
      html_base = html_base.replace("xformatx", infoAboutRelease.getFormat());
      html_base = html_base.replace("xbitratex", infoAboutRelease.getBitrate());
      html_base = html_base.replace("xsampleratex", infoAboutRelease.getSampleRate());
      html_base = html_base.replace("xsizex", infoAboutRelease.getSize());
      
      // if (info.getCategory().contains("RECORDPOOL")) {
      //    String downloadID = DOWNLOAD_POSTER.addDownload(info.getReleaseName(),
      //      info.getBoxComDownloadLink());
      //    String template = "https://myrecordpool.com/?smd_process_download=1&download_id={0}";
      //    String link = MessageFormat.format(template, downloadID);
      //    html_base = html_base.replace("xlinkx", link);
      // } else {
      html_base = html_base.replace("xlinkx", info.getBoxComDownloadLink());
      // }
      
      List<Track> TrackList = infoAboutRelease.getTrackList();
      StringBuilder trackList = new StringBuilder();
      trackList.append("\r\n");
      for (int i = 0; i < TrackList.size(); i++) {
         Track track = TrackList.get(i);
         trackList.append("<tr>");
         trackList.append("\r\n");
         append(trackList, Integer.toString(i + 1));
         append(trackList, track.getTitle());
         append(trackList, track.getArtist());
         append(trackList, track.getTrackDuration());
         trackList.append("</tr>");
      }
      html_base = html_base.replace("xtracklistx", trackList.toString());
      
      return html_base;
   }
   
   @SuppressWarnings("unchecked")
   default Integer[] getCategoryIDsForPost(Release info) {
      String releaseName = info.getReleaseName();
      String category = info.getCategory();
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
         case "SCENE-MVID":
            categories.add(Integer.valueOf("78589"));
            setCategoriesForBeatAndScene(info, categories);
            break;
      }
      return categories.toArray(new Integer[0]);
   }
   
   default void setCategoriesForBeatAndScene(Release info, List<Integer> categories) {
      if (!info.getInfoAboutRelease().getGenre().equals("Mixed")) {
         String genreFiltered = info.getInfoAboutRelease().getGenre()
           .replaceAll("\\)", "").replaceAll("\\(", "")
           .replaceAll("^[0-9]", "").trim();
         if (!genreFiltered.equals("")) {
            Integer genre = createCategory(genreFiltered, "5514");
            categories.add(genre);
         }
      }
      // String artistInfo = info.getArtist().trim();
      // if (!artistInfo.equals("") && !artistInfo.equals(" ")) {
      //    Integer artist = createCategory(artistInfo, "5513");
      //    categories.add(artist);
      // }
   }
   
   @SuppressWarnings("unchecked")
   default void setCategories(String releaseName, List<Integer> categories, String category) {
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
   default Integer createCategory(String category, String parentId) {
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
   
   default void append(StringBuilder trackList, String valueString) {
      trackList.append("<td>");
      trackList.append(valueString);
      trackList.append("</td>");
      trackList.append("\r\n");
   }
   
   @SneakyThrows
   static String getClean(String url, String cookie) {
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      HttpGet get = new HttpGet(url);
      get.setHeader("cookie", cookie);
      @Cleanup CloseableHttpResponse response = client.execute(get);
      return EntityUtils.toString(response.getEntity());
   }
   
   static String postClean(String url, String cookie, String body) throws IOException {
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      
      HttpPost get = new HttpPost(url);
      get.setHeader("cookie", cookie);
      
      get.addHeader("Content-Type", " application/x-www-form-urlencoded; charset=UTF-8");
      get.setEntity(new StringEntity(body, ContentType.APPLICATION_FORM_URLENCODED));
      @Cleanup CloseableHttpResponse response = client.execute(get);
      
      return EntityUtils.toString(response.getEntity());
   }
}
