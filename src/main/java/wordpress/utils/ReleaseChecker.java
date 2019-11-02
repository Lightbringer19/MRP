package wordpress.utils;

import mongodb.MongoControl;
import utils.Constants;
import utils.FUtils;

import java.io.File;

import static com.mongodb.client.model.Filters.eq;

public class ReleaseChecker {
   
   public static void main(String[] args) {
      MongoControl MONGO_CONTROL = new MongoControl();
      String path = "Z:\\TEMP FOR LATER\\2019\\";
      File[] dateFolders = new File(path).listFiles();
      for (File dateFolder : dateFolders) {
         if (!dateFolder.getName().equals("11-02-19")
           || !dateFolder.getName().equals("11-01-19")) {
            File[] categoryFolders = dateFolder.listFiles();
            for (File categoryFolder : categoryFolders) {
               File[] releases = categoryFolder.listFiles();
               for (File release : releases) {
                  String releasePath = release.getAbsolutePath();
                  boolean releaseInDB = MONGO_CONTROL.releasesCollection
                    .find(eq("pathToLocalFolder", releasePath)).first() != null;
                  String name = release.getName();
                  if (!releaseInDB) {
                     System.out.println("Adding release: " + name);
                     if (!categoryFolder.getName().contains("SCENE")) {
                        FUtils.writeFile(Constants.tagsDir, name + ".json",
                          releasePath);
                     } else {
                        FUtils.writeFile(Constants.uploadDir, name + ".json",
                          releasePath);
                     }
                  } else {
                     System.out.println("Releases in DB: " + name);
                  }
               }
            }
         }
      }
   }
}
