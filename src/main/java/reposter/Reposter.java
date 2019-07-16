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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static wordpress.WP_API.postAndGetResponse;

public class Reposter extends Thread {

    private static String AUTHORIZATION_HEADER;
    private static String mainCategory;
    private static MongoControl mongoControl;

    public static void main(String[] args) {
        repostMain();
    }

    @SneakyThrows
    private static void repostMain() {
        Thread.sleep(1000);
        Log.write(CheckDate.getNowTime() + " Reposter Start", "Reposter");
        YamlConfig yamlConfig = new YamlConfig();
        AUTHORIZATION_HEADER = yamlConfig.config.getRepost_authorization();
        mongoControl = new MongoControl();
        BlogPoster blogPoster = new BlogPoster();
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
                    List<String> categoriesIDList = new ArrayList<>();
                    setCategoriesLists(release, categoriesForBlog, categoriesIDList);
                    Log.write("Reposting to WP: " + releaseName, "Reposter");
                    //compose the post
                    String[] categoriesID = categoriesIDList.toArray(new String[0]);
                    String contentMessage = release.getTrackList()
                            + "<p>Download here:<br><a href=\"" + release.getMrpPostLink()
                            + "\">" + release.getMrpPostLink() + "</a></p>";
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
                            Log.write("Not reposted: " + releaseName + " "
                                    + responseInfo.getCode(), "Reposter");
                            if (mainCategory.equals("BEATPORT") && responseInfo.getCode() == 526) {
                                Log.write("Beatport 526 error WP repost Skipped",
                                        "Reposter");
                                break;
                            }
                            Thread.sleep(5000);
                        } else {
                            break;
                        }
                    }
                    Log.write("Reposted to WP: " + releaseName, "Reposter");
                    //make repost to Blogger
                    Log.write("Making Repost to Blog", "Reposter");
                    blogPoster.post(contentMessage, releaseName, categoriesForBlog, mainCategory);
                    Log.write("Reposted to blog: " + releaseName, "Reposter");
                    //make repost to Reddit
                    String content = MessageFormat.format("{0}\n\nDownload here:\n\n{1}",
                            release.getRedditTrackList(), release.getMrpPostLink());
                    Log.write("Making Repost to Reddit", "Reposter");
                    redditPoster.post(mainCategory, releaseName, content);
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

    private static void setCategoriesLists(Release release, List<String> categoriesForBlog,
                                           List<String> categoriesIDList) throws ParseException {
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

    private static String getOrCreateCategory(String categoryToFind) throws ParseException {
        String apiURI = "https://recordpoolmusic.com/wp-json/wp/v2/categories";
        if (mainCategory.contains("SCENE")) {
            apiURI = apiURI.replace("recordpoolmusic", "scenedownload");
        } else if (mainCategory.contains("BEATPORT")) {
            apiURI = apiURI.replace("recordpoolmusic", "beatportmusic");
        }
        ResponseInfo responseInfo = postAndGetResponse(new Document()
                        .append("name", categoryToFind).toJson(), apiURI,
                AUTHORIZATION_HEADER);
        String categoryID = null;
        if (responseInfo.getCode() == 400) {
            // cant find or create category but it exists
            System.out.println("Category found: " + categoryToFind);
            categoryID = ((JSONObject) ((JSONObject) new JSONParser()
                    .parse(responseInfo.getJsonBody())).get("data")).get("term_id").toString();

        } else if (responseInfo.getCode() == 201) {
            System.out.println("Category created: " + categoryToFind);
            categoryID = new Document(Document.parse(responseInfo.getJsonBody()))
                    .get("id").toString();
        }
        return categoryID;
    }

    @Override
    public void run() {
        repostMain();
    }
}
