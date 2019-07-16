package ftp;

import com.mongodb.client.FindIterable;
import mongodb.MongoControl;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import utils.FUtils;
import utils.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
import static utils.Constants.scheduleDir;
import static utils.Constants.tagsDir;
import static utils.FUtils.writeToFile;

public class ScheduleWatcher {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
    public static final MongoControl MONGO_CONTROL = new MongoControl();

    ScheduleWatcher() {
    }

    public static void main(String[] args) throws IOException {
        ScheduleWatcher scheduleWatcher = new ScheduleWatcher();
        scheduleWatcher.scheduleDownloaded();
        scheduleWatcher.addReleaseToTagQue();
        scheduleWatcher.checkToDownloadQueue();
    }

    void checkToDownloadQueue() {
        FindIterable<Document> toDownload = MONGO_CONTROL.toDownloadCollection.find();
        for (Document releaseToDownload : toDownload) {
            String releaseName = (String) releaseToDownload.get("releaseName");
            String link = (String) releaseToDownload.get("link");
            String categoryName = (String) releaseToDownload.get("categoryName");
            writeToFile(releaseName, link, categoryName);
        }
    }

    void addReleaseToTagQue() {
        Document releaseToAdd = MONGO_CONTROL.scheduleCollection
                .find(lt("scheduleTimeMillis", new Date().getTime())).first();
        if (releaseToAdd != null) {
            String releaseName = releaseToAdd.get("releaseName").toString();
            String path = releaseToAdd.get("path").toString();
            Log.write("Adding Release to Tag Que: " + releaseName,
                    "FTP&SCHEDULER");
            FUtils.writeFile(tagsDir, releaseName + ".json", path);
            MONGO_CONTROL.scheduleCollection.deleteOne(releaseToAdd);
        }
    }

    void scheduleDownloaded() throws IOException {
        File[] toSchedule = new File(scheduleDir).listFiles();
        for (File scheduleFile : toSchedule) {
            File folderToSchedule = new File(FUtils.readFile(scheduleFile));
            // LOGIC FOR DIFFERENT FTP
            String categoryName = folderToSchedule.getParentFile().getName();
            if (categoryName.equals("RECORDPOOL")) {
                // add to schedule DB
                addToScheduleDB(folderToSchedule);

            } else {
                // add to tag editor SCHEDULE
                File tagScheduleFile = new File(tagsDir +
                        scheduleFile.getName());
                FileUtils.copyFile(scheduleFile, tagScheduleFile);
            }
            // delete from TO_DOWNLOAD QUEUE
            MONGO_CONTROL.toDownloadCollection
                    .deleteOne(eq("releaseName", folderToSchedule.getName()));
            // add releases to FTP_Downloaded DB
            MONGO_CONTROL.rpDownloadedCollection
                    .insertOne(new Document("releaseName", folderToSchedule.getName()));
            //delete schedule file
            scheduleFile.delete();
        }
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
                + " To Time: " + scheduleTime, "FTP&SCHEDULER");
        MONGO_CONTROL.scheduleCollection.insertOne(scheduleTask);
    }
}
