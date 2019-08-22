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
import static wordpress.Poster.MONGO_CONTROL;
import static wordpress.Poster.MRP_AUTHORIZATION;

public class WP_API {
    
    public static void main(String[] args) {
    }
    
    static void post(File jsonFile) {
        InfoForPost info = collect(jsonFile);
        String htmlBodyForPost = buildHTML(info);
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
    
    private static String createPostGetLinkToPost(String JSON_BODY, String releaseName,
                                                  String apiURI, String authorizationHeader) {
        try {
            while (true) {
                ResponseInfo response = postAndGetResponse(JSON_BODY, apiURI, authorizationHeader);
                if (response.getCode() != 201) {
                    Log.write("Not posted: " + releaseName + " " + response.getCode(),
                       "Poster");
                    Thread.sleep(5000);
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
    
    private static String buildHTML(InfoForPost info) {
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
        html_base = html_base.replace("xlinkx", info.getLink());
        
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
    private static String[] getCategoryIDsForPost(InfoForPost info) {
        String releaseName = info.getReleaseName();
        String category = info.getPostCategory();
        Map<String, String> categoriesAndIDs;
        List<String> categories = new ArrayList<>();
        switch (category) {
            case "BEATPORT":
                category = "115";
                categories.add(category);
                break;
            case "RECORDPOOL MUSIC":
                category = "77";
                categories.add(category);
                categoriesAndIDs = (Map<String, String>)
                   MONGO_CONTROL.categoriesCollection
                      .find(eq("name", "RECORDPOOL MUSIC"))
                      .first().get("categoriesAndIDs");
                for (Map.Entry<String, String> categoryAndID : categoriesAndIDs.entrySet()) {
                    if (releaseName.contains(categoryAndID.getKey())) {
                        categories.add(categoryAndID.getValue());
                        break;
                    }
                }
                break;
            case "RECORDPOOL VIDEOS":
                category = "29";
                categories.add(category);
                categoriesAndIDs = (Map<String, String>)
                   MONGO_CONTROL.categoriesCollection
                      .find(eq("name", "RECORDPOOL VIDEOS"))
                      .first().get("categoriesAndIDs");
                for (Map.Entry<String, String> categoryAndID : categoriesAndIDs.entrySet()) {
                    if (releaseName.contains(categoryAndID.getKey())) {
                        categories.add(categoryAndID.getValue());
                        break;
                    }
                }
                break;
            case "SCENE-MP3":
                category = "184";
                categories.add(category);
                break;
            case "SCENE-FLAC":
                category = "191";
                categories.add(category);
                break;
        }
        return categories.toArray(new String[0]);
    }
    
    private static void append(StringBuilder trackList, String valueString) {
        trackList.append("<td>");
        trackList.append(valueString);
        trackList.append("</td>");
        trackList.append("\r\n");
    }
}
