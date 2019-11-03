package wordpress.utils;

import mongodb.MongoControl;
import utils.Constants;
import utils.FUtils;

import java.io.File;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;

public class ReleaseChecker {
   private static Scanner IN;
   
   public static void main(String[] args) {
      IN = new Scanner(System.in);
      MongoControl MONGO_CONTROL = new MongoControl();
      System.out.println("Enter day folder path: ");
      String path = IN.nextLine();
      File dateFolder = new File(path);
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
