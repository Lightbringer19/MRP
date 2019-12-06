package utils.manual;

import com.mongodb.client.MongoCollection;
import mongodb.MongoControl;
import org.bson.Document;
import utils.FUtils;

import java.io.File;

import static com.mongodb.client.model.Filters.eq;
import static utils.Constants.tagsDir;

public interface ManualSchedulerInterface {
   
   MongoControl MONGO_CONTROL = new MongoControl();
   MongoCollection<Document> RELEASES_COLLECTION = MONGO_CONTROL.releasesCollection;
   MongoCollection<Document> SCHEDULE_COLLECTION = MONGO_CONTROL.scheduleCollection;
   
   default void checkRelease(File release) {
      String releaseName = release.getName();
      if (releaseNotInDB(RELEASES_COLLECTION, releaseName)) {
         if (releaseNotInDB(SCHEDULE_COLLECTION, releaseName)) {
            FUtils.writeFile(tagsDir.replace("\\Utils", ""),
              releaseName + ".json", release.getAbsolutePath());
            System.out.println("Release added to tag Queue: " + releaseName);
         } else {
            System.out.println("Release in Schedule Queue: " + releaseName);
         }
      } else {
         System.out.println("Release was Posted: " + releaseName);
      }
   }
   
   default boolean releaseNotInDB(MongoCollection<Document> releasesCollection, String releaseName) {
      return releasesCollection
        .find(eq("releaseName", releaseName)).first() == null;
   }
}
