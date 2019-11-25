package scheduler;

import lombok.SneakyThrows;
import mongodb.MongoControl;
import org.bson.Document;
import utils.FUtils;
import utils.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.mongodb.client.model.Filters.lt;
import static utils.Constants.postDir;

public class ScheduleWatcher extends Thread {
   
   public static final SimpleDateFormat DATE_FORMAT =
     new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
   private static MongoControl MONGO_CONTROL;
   
   public ScheduleWatcher() {
      MONGO_CONTROL = new MongoControl();
   }
   
   @Override
   public void run() {
      Timer timer = new Timer();
      ScheduleWatcher scheduleWatcher = new ScheduleWatcher();
      TimerTask addReleaseToTagQue = new TimerTask() {
         @Override
         @SneakyThrows
         public void run() {
            scheduleWatcher.postRelease();
         }
      };
      long sec = 1000;
      long min = sec * 60;
      long hour = 60 * min;
      timer.schedule(addReleaseToTagQue, 0, min);
   }
   
   private void postRelease() {
      Document releaseToPost = MONGO_CONTROL.scheduleCollection
        .find(lt("scheduleTimeMillis", new Date().getTime())).first();
      if (releaseToPost != null) {
         String releaseName = releaseToPost.get("releaseName").toString();
         Log.write("Adding Release to Post Que: " + releaseName,
           "SCHEDULER");
         FUtils.writeFile(postDir + "RECORDPOOL", releaseName, "");
         MONGO_CONTROL.scheduleCollection.deleteOne(releaseToPost);
      }
   }
}
