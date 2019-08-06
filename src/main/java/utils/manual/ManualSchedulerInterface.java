package utils.manual;

import com.mongodb.client.MongoCollection;
import mongodb.MongoControl;
import org.bson.Document;

import java.io.File;

import static com.mongodb.client.model.Filters.eq;
import static ftp.ScheduleWatcher.addToScheduleDB;

public interface ManualSchedulerInterface {
    
    MongoControl MONGO_CONTROL = new MongoControl();
    MongoCollection<Document> RELEASES_COLLECTION = MONGO_CONTROL.releasesCollection;
    MongoCollection<Document> SCHEDULE_COLLECTION = MONGO_CONTROL.scheduleCollection;
    MongoCollection<Document> TO_DOWNLOAD_COLLECTION = MONGO_CONTROL.toDownloadCollection;
    
    default void checkRelease(File release) {
        String releaseName = release.getName();
        System.out.println("Checking: " + releaseName);
        if (!releaseInDB(RELEASES_COLLECTION, releaseName)) {
            if (!releaseInDB(SCHEDULE_COLLECTION, releaseName)) {
                if (!releaseInDB(TO_DOWNLOAD_COLLECTION, releaseName)) {
                    addToScheduleDB(release);
                } else {
                    System.out.println("Release in To_Download Queue: " + releaseName);
                }
            } else {
                System.out.println("Release in Schedule Queue: " + releaseName);
            }
        } else {
            System.out.println("Release was Posted: " + releaseName);
        }
    }
    
    default boolean releaseInDB(MongoCollection<Document> releasesCollection, String releaseName) {
        return releasesCollection
           .find(eq("releaseName", releaseName)).first() != null;
    }
}