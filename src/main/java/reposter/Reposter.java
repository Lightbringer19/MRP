package reposter;

import com.google.gson.Gson;
import json.db.ReleaseNew;
import lombok.SneakyThrows;
import mongodb.MongoControl;
import org.bson.Document;
import org.bson.types.ObjectId;
import utils.CheckDate;
import utils.Log;

import static com.mongodb.client.model.Filters.eq;
import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.joining;

public class Reposter extends Thread {
   
   private static MongoControl mongoControl;
   
   // public static void main(String[] args) {
   //    repostMain();
   // }
   
   @SneakyThrows
   private void repostMain() {
      Thread.sleep(1000);
      Log.write(CheckDate.getNowTime() + " Reposter Start", "Reposter");
      mongoControl = new MongoControl();
      RedditPoster redditPoster = new RedditPoster();
      while (true) {
         try {
            Document taskDoc = mongoControl.tasksCollection
              .find(eq("task", "repost")).first();
            if (taskDoc != null) {
               ReleaseNew release = getRelease(taskDoc);
               String releaseName = release.getReleaseName();
               String category = release.getCategory();
               Log.write("New Repost: " + releaseName + " In: " + category,
                 "Reposter");
               // make repost to Reddit
               Log.write("Making Repost to Reddit", "Reposter");
               String trackList = release.getTrackList().stream().map(track ->
                 format("{0} - {1} ({2})",
                   track.getArtist(), track.getTitle(), track.getTrackDuration()))
                 .collect(joining("\n" + "\n"));
               String downloadLink = "https://myrecordpool.com/release/" + releaseName;
               String contentPattern = "{0}\n\nMore Info Here:\n\n{1}";
               String content = format(contentPattern, trackList, downloadLink);
               redditPoster.post(category, releaseName, content);
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
   
   public static ReleaseNew getRelease(Document taskDoc) {
      ObjectId releaseId = new ObjectId(taskDoc.get("releaseId").toString());
      Document foundDoc = mongoControl.releasesNewCollection
        .find(eq("_id", releaseId)).first();
      foundDoc.remove("_id");
      foundDoc.remove("date");
      return new Gson().fromJson(foundDoc.toJson(), ReleaseNew.class);
   }
   
   @Override
   public void run() {
      repostMain();
   }
}
