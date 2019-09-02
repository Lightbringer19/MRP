package scheduler;

import lombok.SneakyThrows;
import mongodb.MongoControl;
import org.bson.Document;
import utils.FUtils;
import utils.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.mongodb.client.model.Filters.lt;
import static utils.Constants.tagsDir;

public class ScheduleWatcher extends Thread {
   
   private static final SimpleDateFormat DATE_FORMAT =
     new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
   private static final MongoControl MONGO_CONTROL = new MongoControl();
   
   public ScheduleWatcher() {
   }
   
   public static void addToScheduleDB(File folderToSchedule) {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MINUTE, ThreadLocalRandom.current().nextInt(480, 720));
      String scheduleTime = DATE_FORMAT.format(cal.getTime());
      Document scheduleTask = new Document()
        .append("releaseName", folderToSchedule.getName())
        .append("path", folderToSchedule.getAbsolutePath())
        .append("scheduleTime", scheduleTime)
        .append("scheduleTimeMillis", cal.getTime().getTime());
      Log.write("Scheduling the Release: " + folderToSchedule.getName()
        + " To Time: " + scheduleTime, "SCHEDULER");
      MONGO_CONTROL.scheduleCollection.insertOne(scheduleTask);
   }
   
   @Override
   public void run() {
      Timer timer = new Timer();
      ScheduleWatcher scheduleWatcher = new ScheduleWatcher();
      TimerTask addReleaseToTagQue = new TimerTask() {
         @Override
         @SneakyThrows
         public void run() {
            scheduleWatcher.addReleaseToTagQue();
         }
      };
      long sec = 1000;
      long min = sec * 60;
      long hour = 60 * min;
      timer.schedule(addReleaseToTagQue, 0, min);
   }
   
   private void addReleaseToTagQue() {
      Document releaseToAdd = MONGO_CONTROL.scheduleCollection
        .find(lt("scheduleTimeMillis", new Date().getTime())).first();
      if (releaseToAdd != null) {
         String releaseName = releaseToAdd.get("releaseName").toString();
         String path = releaseToAdd.get("path").toString();
         Log.write("Adding Release to Tag Que: " + releaseName,
           "SCHEDULER");
         FUtils.writeFile(tagsDir, releaseName + ".json", path);
         MONGO_CONTROL.scheduleCollection.deleteOne(releaseToAdd);
      }
   }
}
