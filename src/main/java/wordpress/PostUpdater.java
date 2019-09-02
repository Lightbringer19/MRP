package wordpress;

import configuration.YamlConfig;
import json.ResponseInfo;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import utils.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static wordpress.Poster.MONGO_CONTROL;
import static wordpress.Poster.MRP_AUTHORIZATION;
import static wordpress.WP_API.createCategory;
import static wordpress.WP_API.postAndGetResponse;

public class PostUpdater extends Thread {
   
   private static Logger logger = new Logger("Post Updater");
   
   public PostUpdater() {
      YamlConfig yamlConfig = new YamlConfig();
      MRP_AUTHORIZATION = yamlConfig.config.getMrp_authorization();
   }
   
   public static void main(String[] args) {
      PostUpdater postUpdater = new PostUpdater();
      postUpdater.start();
      
   }
   
   @Override
   @SneakyThrows
   public void run() {
      for (int postId = 115003; postId < 204958; postId++) {
         try {
            updateOnePost(postId);
         } catch (Exception e) {
            logger.log(e);
            Thread.sleep(5000);
         }
      }
   }
   
   @SuppressWarnings({"unchecked", "DuplicatedCode"})
   private void updateOnePost(int postId) throws IOException {
      logger.log("Preparing       : (" + postId + ")");
      String url = "https://myrecordpool.com/wp-json/wp/v2/posts/" + postId;
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      HttpGet get = new HttpGet(url);
      @Cleanup CloseableHttpResponse response = client.execute(get);
      int responseCode = response.getStatusLine().getStatusCode();
      if (responseCode == 200) {
         String clean = EntityUtils.toString(response.getEntity());
         Document post = new Document(Document.parse(clean));
         List<Integer> categories = (List<Integer>) post.get("categories");
         if (categories.size() == 1) {
            Integer mainCategory = categories.get(0);
            Document content = (Document) post.get("content");
            String rendered = content.get("rendered").toString();
            if (rendered.contains("<td>Artist:</td>")) {
               String releaseName = ((Document) post.get("title")).get("rendered").toString();
               logger.log("Updating        : (" + postId + ") | " + releaseName);
               if (mainCategory == 77 || mainCategory == 29) {
                  //RECORDPOOL
                  setRecordpoolCategories(categories, mainCategory, releaseName);
               } else {
                  //SCENE & BEATPORT
                  org.jsoup.nodes.Document postContent = Jsoup.parse(rendered);
                  String artistName = postContent.select("tr:contains(Artist)").select("td").get(1).text();
                  String genreName = postContent.select("tr:contains(Genre)").select("td").get(1).text();
                  setCategories(categories, artistName, genreName);
               }
               String updatedPostLink = updatePost(
                 new Document("categories", categories).toJson(), postId, url);
            }
         }
      } else {
         logger.log("Error on search : " + postId + " |: " + responseCode);
      }
   }
   
   @SuppressWarnings("unchecked")
   private void setRecordpoolCategories(List<Integer> categories, Integer mainCategory, String releaseName) {
      String category;
      if (mainCategory == 77) {
         category = "RECORDPOOL MUSIC";
      } else {
         category = "RECORDPOOL VIDEOS";
      }
      Map<String, String> categoriesAndIDs;
      categoriesAndIDs = (Map<String, String>)
        MONGO_CONTROL.categoriesCollection
          .find(eq("name", category))
          .first().get("categoriesAndIDs");
      for (Map.Entry<String, String> categoryAndID : categoriesAndIDs.entrySet()) {
         if (releaseName.contains(categoryAndID.getKey())) {
            categories.add(Integer.parseInt(categoryAndID.getValue()));
            break;
         }
      }
   }
   
   private void setCategories(List<Integer> categories, String artistName, String genreName) {
      if (!genreName.equals("Mixed")) {
         String genreFiltered = genreName
           .replaceAll("\\)", "").replaceAll("\\(", "")
           .replaceAll("^[0-9]", "").trim();
         String genre = createCategory(genreFiltered, "5514");
         categories.add(Integer.parseInt(genre));
      }
      String artistInfo = artistName.trim();
      if (!artistInfo.equals("") && !artistInfo.equals(" ")) {
         String artist = createCategory(artistInfo, "5513");
         categories.add(Integer.parseInt(artist));
      }
   }
   
   private String updatePost(String JSON_BODY, int postID, String apiURI) {
      try {
         while (true) {
            ResponseInfo response = postAndGetResponse(JSON_BODY, apiURI, MRP_AUTHORIZATION);
            if (response.getCode() != 200) {
               logger.log("Not updated     : " + postID + " |: " + response.getCode());
               Thread.sleep(5000);
            } else {
               JSONObject post = (JSONObject) new JSONParser().parse(response.getJsonBody());
               logger.log("Post updated    : " + postID);
               return ((JSONObject) post.get("guid")).get("raw").toString();
            }
         }
      } catch (Exception e) {
         logger.log(e);
      }
      return null;
   }
}
