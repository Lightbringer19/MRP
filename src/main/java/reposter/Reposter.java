package reposter;

import com.google.gson.Gson;
import configuration.YamlConfig;
import json.ResponseInfo;
import json.WPPost;
import json.db.Release;
import lombok.SneakyThrows;
import mongodb.MongoControl;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.CheckDate;
import utils.Log;
import wordpress.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class Reposter extends Thread implements ApiInterface {
   
   private static String AUTHORIZATION_HEADER;
   private static String mainCategory;
   private static MongoControl mongoControl;
   
   // public static void main(String[] args) {
   //    repostMain();
   // }
   
   @SneakyThrows
   private void repostMain() {
      Thread.sleep(1000);
      Log.write(CheckDate.getNowTime() + " Reposter Start", "Reposter");
      YamlConfig yamlConfig = new YamlConfig();
      AUTHORIZATION_HEADER = yamlConfig.config.getRepost_authorization();
      mongoControl = new MongoControl();
      // BlogPoster blogPoster = new BlogPoster();
      RedditPoster redditPoster = new RedditPoster();
      while (true) {
         try {
            Document taskDoc = mongoControl.tasksCollection
              .find(eq("task", "repost")).first();
            if (taskDoc != null) {
               Release release = getRelease(taskDoc);
               String releaseName = release.getReleaseName();
               mainCategory = release.getCategory();
               Log.write("New Repost: " + releaseName + " In: " + mainCategory,
                 "Reposter");
               //categories
               Log.write("Getting categories for: " + releaseName, "Reposter");
               List<String> categoriesForBlog = new ArrayList<>();
               List<Integer> categoriesIDList = new ArrayList<>();
               setCategoriesLists(release, categoriesForBlog, categoriesIDList);
               Log.write("Reposting to WP: " + releaseName, "Reposter");
               //compose the post
               Integer[] categoriesID = categoriesIDList.toArray(new Integer[0]);
               // String contentMessage = release.getTrackList()
               //   + "<p>More Info Here:<br><a href=\"" + release.getMrpPostLink()
               //   + "\">" + release.getMrpPostLink() + "</a></p>";
               String contentMessage = null;
               WPPost post = new WPPost(releaseName, "publish",
                 categoriesID, contentMessage);
               //make post
               String apiURI = "https://recordpoolmusic.com/wp-json/wp/v2/posts";
               if (mainCategory.contains("SCENE")) {
                  apiURI = apiURI
                    .replace("recordpoolmusic", "scenedownload");
               } else if (mainCategory.contains("BEATPORT")) {
                  apiURI = apiURI.replace("recordpoolmusic", "beatportmusic");
               }
               while (true) {
                  ResponseInfo responseInfo = postAndGetResponse(post.toJson(), apiURI,
                    AUTHORIZATION_HEADER);
                  if (responseInfo.getCode() != 201) {
                     Log.write("Not reposted: " + mainCategory + "| " + releaseName +
                       " Code: "
                       + responseInfo.getCode(), "Reposter");
                     System.out.println(responseInfo.getJsonBody());
                     // if (mainCategory.equals("BEATPORT") && responseInfo.getCode() == 526) {
                     //     Log.write("Beatport 526 error WP repost Skipped",
                     //        "Reposter");
                     //     break;
                     // } else if (responseInfo.getCode() == 400) {
                     //     Log.write("Error 400 WP repost Skipped", "Reposter");
                     //     break;
                     // }
                     Thread.sleep(7000);
                  } else {
                     break;
                  }
               }
               Log.write("Reposted to WP: " + releaseName, "Reposter");
               //make repost to Blogger
               // Log.write("Making Repost to Blog", "Reposter");
               // blogPoster.post(contentMessage, releaseName, categoriesForBlog, mainCategory);
               // Log.write("Reposted to blog: " + releaseName, "Reposter");
               //make repost to Reddit
               // String content = MessageFormat.format("{0}\n\nMore Info Here:\n\n{1}",
               //   release.getRedditTrackList(), release.getMrpPostLink());
               // Log.write("Making Repost to Reddit", "Reposter");
               // redditPoster.post(mainCategory, releaseName, content);
               Log.write("Reposted to Reddit: " + releaseName, "Reposter");
               // delete task from que in the end
               mongoControl.tasksCollection.deleteOne(taskDoc);
            } else {
               Log.write(CheckDate.getNowTime() + " REPOSTER SLEEPING",
                 "Reposter");
               Thread.sleep(10000);
            }
         } catch (Exception e) {
            Log.write(e + " Exception Reposter", "Reposter");
            Log.write(e, "Reposter");
            Thread.sleep(10000);
         }
      }
   }
   
   private void setCategoriesLists(Release release, List<String> categoriesForBlog,
                                   List<Integer> categoriesIDList) throws ParseException {
      categoriesForBlog.add(mainCategory);
      categoriesIDList.add(getOrCreateCategory(mainCategory));
      if (mainCategory.contains("SCENE")) {
         String genre = release.getInfoAboutRelease().getGenre()
           .replaceAll("[^a-zA-Z]", "");
         categoriesIDList.add(getOrCreateCategory(release.getInfoAboutRelease().getArtist()));
         categoriesIDList.add(getOrCreateCategory(release.getInfoAboutRelease().getGroup()));
         categoriesIDList.add(getOrCreateCategory(genre));
         categoriesForBlog.add(genre);
      }
   }
   
   private static Release getRelease(Document taskDoc) {
      ObjectId releaseId = new ObjectId(taskDoc.get("releaseId").toString());
      // use extracted ID to find DOC in main DB and cast to Object
      Document foundDoc = mongoControl.releasesCollection
        .find(eq("_id", releaseId)).first();
      foundDoc.remove("_id");
      return new Gson().fromJson(foundDoc.toJson(), Release.class);
   }
   
   @SneakyThrows
   private Integer getOrCreateCategory(String categoryToFind) {
      String apiURI = "https://recordpoolmusic.com/wp-json/wp/v2/categories";
      if (mainCategory.contains("SCENE")) {
         apiURI = apiURI.replace("recordpoolmusic", "scenedownload");
      } else if (mainCategory.contains("BEATPORT")) {
         apiURI = apiURI.replace("recordpoolmusic", "beatportmusic");
      }
      while (true) {
         ResponseInfo responseInfo = postAndGetResponse(new Document()
             .append("name", categoryToFind).toJson(), apiURI,
           AUTHORIZATION_HEADER);
         String categoryID;
         if (responseInfo.getCode() == 400) {
            // cant find or create category but it exists
            categoryID = ((JSONObject) ((JSONObject) new JSONParser()
              .parse(responseInfo.getJsonBody())).get("data")).get("term_id").toString();
            // Log.write(
            Log.write("Category found: " + categoryToFind + " ID: " + categoryID,
              "Reposter");
            return Integer.valueOf(categoryID);
         } else if (responseInfo.getCode() == 201) {
            categoryID = new Document(Document.parse(responseInfo.getJsonBody()))
              .get("id").toString();
            Log.write("Category created: " + categoryToFind + " ID: " + categoryID,
              "Reposter");
            return Integer.valueOf(categoryID);
         } else {
            Log.write("Error on search: " + categoryToFind +
                " RESPONSE CODE: " + responseInfo.getCode(),
              "Reposter");
            System.out.println(responseInfo.getJsonBody());
            Thread.sleep(3000);
         }
      }
      
   }
   
   @Override
   public void run() {
      repostMain();
   }
}
