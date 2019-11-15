package wordpress;

import com.google.gson.Gson;
import configuration.YamlConfig;
import json.InfoFromBoxCom;
import mongodb.MongoControl;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import utils.CheckDate;
import utils.Constants;
import utils.FUtils;
import utils.Log;
import wordpress.utils.DownloadPoster;

import java.io.File;
import java.io.IOException;

public class Poster extends Thread {
   public static MongoControl MONGO_CONTROL;
   
   public static String MRP_AUTHORIZATION;
   public static DownloadPoster DOWNLOAD_POSTER;
   
   @Override
   public void run() {
      Log.write(CheckDate.getNowTime() + " Poster Start", "Poster");
      MONGO_CONTROL = new MongoControl();
      DOWNLOAD_POSTER = new DownloadPoster();
      new File(Constants.postDir).mkdirs();
      YamlConfig yamlConfig = new YamlConfig();
      MRP_AUTHORIZATION = yamlConfig.config.getMrp_authorization();
      while (true) {
         try {
            for (File categoryFolder : new File(Constants.postDir).listFiles()) {
               for (File postJsonFile : categoryFolder.listFiles()) {
                  String category = postJsonFile.getParentFile().getName();
                  Log.write("Processing: " + postJsonFile.getName()
                    + "| In: " + category, "Poster");
                  // check for corrupted files in folder
                  findAndDeleteCorruptedIn(postJsonFile);
                  // check for folder without audio files
                  if (musicIsHere(postJsonFile)) {
                     // make post
                     Log.write("Preparing to post: " + postJsonFile.getName()
                       + "| In: " + category, "Poster");
                     if (groupCheck(postJsonFile)) {
                        WP_API.post(postJsonFile);
                        postJsonFile.delete();
                     } else {
                        Log.write("Not SRC or gFViD group: " + postJsonFile.getName()
                          + " | In: " + category, "Poster");
                        FileUtils.moveFileToDirectory(postJsonFile,
                          new File(Constants.filesDir + "NOT-POSTED\\"), true);
                     }
                     Thread.sleep(2000);
                  } else {
                     postJsonFile.delete();
                     Log.write("Deleted(no audio): " + postJsonFile.getName()
                       + "| In: " + category, "Poster");
                  }
               }
            }
            Log.write(CheckDate.getNowTime() + " POSTER SLEEPING", "Poster");
            Sleep();
         } catch (Exception e) {
            Log.write(CheckDate.getNowTime() + " EXCEPTION " + e, "Poster");
            Sleep();
         }
      }
   }
   
   private boolean groupCheck(File postJsonFile) {
      File folderToCollect = getFolderFromPostInfo(postJsonFile);
      String postCategory = getCategory(folderToCollect);
      if (postCategory.equals("SCENE-MVID")) {
         String[] folderElements = folderToCollect.getName().split("-");
         String Group = folderElements[folderElements.length - 1];
         return Group.toLowerCase().contains("srp")
           || Group.toLowerCase().contains("gfvid");
      }
      return true;
   }
   
   private static void findAndDeleteCorruptedIn(File post) {
      File[] audioFiles = getAudioFiles(post);
      if (audioFiles != null && audioFiles.length > 0) {
         for (File aFile : audioFiles) {
            try {
               AudioFile audioFile = AudioFileIO.read(aFile);
            } catch (InvalidAudioFrameException | CannotReadException e) {
               Log.write("Corrupted file deleted: " + aFile.getName(),
                 "Poster");
               aFile.delete();
            } catch (IOException | TagException | ReadOnlyFileException e) {
               Log.write("findAndDeleteCorruptedIn Exception: " + e,
                 "Poster");
            }
         }
      }
   }
   
   private static File[] getAudioFiles(File post) {
      File folderToCollect = getFolderFromPostInfo(post);
      String postCategory = getCategory(folderToCollect);
      return getFiles(folderToCollect, postCategory);
   }
   
   private static File[] getFiles(File folderToCollect, String postCategory) {
      File[] audioFiles = null;
      if (!postCategory.equals("RECORDPOOL VIDEOS") && !postCategory.equals("SCENE-MVID")) {
         audioFiles = folderToCollect.listFiles((dir, name) -> {
            if (postCategory.equals("SCENE-FLAC")) {
               return name.toLowerCase().endsWith(".flac");
            } else {
               return name.toLowerCase().endsWith(".mp3");
            }
         });
      }
      return audioFiles;
   }
   
   private static boolean musicIsHere(File post) {
      File folderToCollect = getFolderFromPostInfo(post);
      String postCategory = getCategory(folderToCollect);
      boolean filesHere;
      if (postCategory.equals("RECORDPOOL VIDEOS") || postCategory.equals("SCENE-MVID")) {
         filesHere = true;
      } else {
         File[] audioFiles = getAudioFiles(post);
         filesHere = audioFiles.length > 0;
      }
      return filesHere;
   }
   
   public static String getCategory(File folderToCollect) {
      String category = folderToCollect.getParentFile().getName();
      if (category.equals("RECORDPOOL")) {
         category = getRecordPoolCategory(folderToCollect, category);
      }
      return category;
   }
   
   public static String getRecordPoolCategory(File folderToCollect, String category) {
      File[] RecordPoolFolder = folderToCollect.listFiles();
      for (File RPfile : RecordPoolFolder) {
         if (RPfile.getName().toLowerCase().endsWith(".mp3")) {
            category = "RECORDPOOL MUSIC";
            break;
         }
         if (RPfile.getName().toLowerCase().endsWith(".mp4")) {
            category = "RECORDPOOL VIDEOS";
            break;
         }
      }
      return category;
   }
   
   private static File getFolderFromPostInfo(File post) {
      String localPathToFolder = new Gson().fromJson(FUtils.readFile(post),
        InfoFromBoxCom.class).getLocalPathToFolder();
      return new File(localPathToFolder);
   }
   
   private static void Sleep() {
      try {
         Thread.sleep(10 * 1000);
      } catch (InterruptedException ignored) {
      }
   }
}
