package collector;

import com.google.gson.Gson;
import configuration.YamlConfig;
import json.InfoForPost;
import json.InfoFromBoxCom;
import json.ResponseInfo;
import json.db.InfoAboutRelease;
import json.db.Release;
import mongodb.MongoControl;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import utils.*;
import wordpress.PosterInterface;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static collector.ImageCompressor.compress;
import static collector.ImageUploader.uploadImage;
import static com.mongodb.client.model.Filters.eq;
import static scheduler.ScheduleWatcher.DATE_FORMAT;
import static utils.Constants.filesDir;
import static utils.Constants.postDir;
import static wordpress.Poster.MRP_AUTHORIZATION;

public class Collector extends Thread implements CollectorInterface, PosterInterface {
   
   private MongoControl MONGO_CONTROL;
   
   protected Logger logger = new Logger("Collector");
   
   @Override
   public void run() {
      MONGO_CONTROL = new MongoControl();
      YamlConfig yamlConfig = new YamlConfig();
      String mrp_pc_api = yamlConfig.config.getMrp_pc_api();
      MRP_AUTHORIZATION = yamlConfig.config.getMrp_authorization();
      logger.log(CheckDate.getNowTime() + " Collector Start");
      new File(Constants.collectorDir).mkdirs();
      while (true) {
         try {
            for (File categoryFolder : new File(Constants.collectorDir).listFiles()) {
               for (File collectJsonFile : categoryFolder.listFiles()) {
                  File releaseFolder = getFolderFromJson(collectJsonFile);
                  String category = collectJsonFile.getParentFile().getName();
                  logger.log("Processing: " + collectJsonFile.getName()
                    + "| In: " + category);
                  findAndDeleteCorruptedIn(collectJsonFile);
                  if (musicIsHere(collectJsonFile)) {
                     logger.log("Preparing to collect: " + collectJsonFile.getName()
                       + "| In: " + category);
                     if (groupCheck(collectJsonFile)) {
                        try {
                           InfoForPost collectedInfo = collect(collectJsonFile);
                           if (category.contains("RECORDPOOL")) {
                              scheduleRelease(collectedInfo);
                           } else {
                              FUtils.writeFile(postDir + category,
                                collectedInfo.getReleaseName(), "");
                           }
                           ResponseInfo responseInfo =
                             postAndGetResponse(new Document("boxComLink", collectedInfo.getLink())
                                 .append("releaseName", collectedInfo.getReleaseName())
                                 .append("category", collectedInfo.getPostCategory())
                                 .toJson(),
                               mrp_pc_api, "");
                           if (responseInfo.getCode() != 200) {
                              while (true) {
                                 System.out.println("NOT ADDED TO ARCHIVE");
                                 sleep(5000);
                              }
                           }
                           deleteRelease(releaseFolder);
                           collectJsonFile.delete();
                        } catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException e) {
                           logger.log(e);
                           FileUtils.moveFileToDirectory(collectJsonFile,
                             new File(filesDir + "NOT-POSTED\\"), true);
                        }
                     } else {
                        logger.log("Not SRC or gFViD group: " + collectJsonFile.getName()
                          + " | In: " + category);
                        FileUtils.moveFileToDirectory(collectJsonFile,
                          new File(filesDir + "NOT-POSTED\\"), true);
                     }
                     Thread.sleep(2000);
                  } else {
                     logger.log("Deleted(no audio): " + collectJsonFile.getName()
                       + "| In: " + category);
                     deleteRelease(releaseFolder);
                     collectJsonFile.delete();
                  }
               }
            }
            logger.log(CheckDate.getNowTime() + " Collector SLEEPING");
            Sleep();
         } catch (Exception e) {
            logger.log(CheckDate.getNowTime() + " EXCEPTION " + e);
            logger.log(e);
            Sleep();
         }
      }
      
   }
   
   private void deleteRelease(File releaseFolder) {
      logger.log("Deleting directory: " + releaseFolder.getName());
      while (true) {
         try {
            FileUtils.deleteDirectory(releaseFolder);
            logger.log("Deleted: " + releaseFolder.getName());
            break;
         } catch (IOException e) {
            logger.log(e);
            Sleep();
         }
      }
   }
   
   public InfoForPost collect(File infoJsonFile) {
      String readFile = FUtils.readFile(infoJsonFile);
      Gson gson = new Gson();
      InfoFromBoxCom infoFromBoxCom = gson.fromJson(readFile, InfoFromBoxCom.class);
      File folderToCollect = new File(infoFromBoxCom.getLocalPathToFolder());
      String postCategory = getCategory(folderToCollect);
      String artLink = getArt(infoFromBoxCom, folderToCollect, postCategory);
      // get Audio Files
      File[] audioFiles = folderToCollect.listFiles((dir, name) -> {
         switch (postCategory) {
            case "SCENE-MP3":
            case "BEATPORT":
            case "RECORDPOOL MUSIC":
               return name.toLowerCase().endsWith(".mp3");
            case "SCENE-FLAC":
               return name.toLowerCase().endsWith(".flac");
            case "RECORDPOOL VIDEOS":
               return name.toLowerCase().endsWith(".mp4");
         }
         return false;
      });
      // collect info from Audio Files
      logger.log("Collecting Info About Files");
      InfoForPost info = collect(audioFiles, infoFromBoxCom.getReleaseName(),
        infoFromBoxCom.getDownloadLinkBoxCom(), artLink, folderToCollect, postCategory);
      // insert info to DB
      Document foundRelease = MONGO_CONTROL.releasesCollection
        .find(eq("releaseName", info.getReleaseName())).first();
      if (foundRelease == null) {
         InfoAboutRelease infoAboutRelease = convertInfo(info);
         Release release = new Release();
         release.setReleaseName(info.getReleaseName());
         release.setCategory(info.getPostCategory());
         release.setBoxComDownloadLink(info.getLink());
         release.setPathToLocalFolder(folderToCollect.getAbsolutePath());
         release.setInfoAboutRelease(infoAboutRelease);
         MONGO_CONTROL.releasesCollection.insertOne(release.toDoc());
      } else {
         InfoAboutRelease infoAboutRelease = convertInfo(info);
         foundRelease.put("category", info.getPostCategory());
         foundRelease.put("boxComDownloadLink", info.getLink());
         foundRelease.put("infoAboutRelease", infoAboutRelease.toDoc());
         MONGO_CONTROL.releasesCollection.replaceOne((eq("_id",
           foundRelease.getObjectId("_id"))), foundRelease);
      }
      return info;
   }
   
   private String getArt(InfoFromBoxCom infoFromBoxCom, File folderToCollect, String postCategory) {
      // get art
      File[] art = folderToCollect.listFiles((dir, name) ->
        name.toLowerCase().endsWith(".jpg"));
      // upload art
      String artLink = "https://myrecordpool.com/wp-content/images/logo.jpg";
      if (postCategory.contains("RECORDPOOL")) {
         @SuppressWarnings("unchecked")
         Map<String, String> imageMap = (Map<String, String>) MONGO_CONTROL
           .staticImageLinksCollection.find().first().get("imageMap");
         for (Map.Entry<String, String> entry : imageMap.entrySet()) {
            if (infoFromBoxCom.getReleaseName().toLowerCase()
              .contains(entry.getKey().toLowerCase())) {
               artLink = entry.getValue();
            }
         }
      } else if (art.length != 0) {
         File theArt = art[0];
         // compress art
         logger.log("Compressing Art");
         File compressedImage = compress(theArt);
         if (compressedImage == null) {
            logger.log("Corrupted Art deleted");
            theArt.delete();
         } else {
            logger.log("Uploading Art");
            // upload compressed art
            artLink = uploadImage(compressedImage);
            compressedImage.delete();
         }
      }
      return artLink;
   }
   
   private boolean groupCheck(File postJsonFile) {
      File folderToCollect = getFolderFromJson(postJsonFile);
      String postCategory = getCategory(folderToCollect);
      if (postCategory.equals("SCENE-MVID")) {
         String[] folderElements = folderToCollect.getName().split("-");
         String Group = folderElements[folderElements.length - 1];
         return Group.toLowerCase().contains("srp")
           || Group.toLowerCase().contains("gfvid")
           || Group.toLowerCase().contains("iuf");
      }
      return true;
   }
   
   private boolean musicIsHere(File post) {
      File folderToCollect = getFolderFromJson(post);
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
   
   private static File getFolderFromJson(File post) {
      String localPathToFolder = new Gson().fromJson(FUtils.readFile(post),
        InfoFromBoxCom.class).getLocalPathToFolder();
      return new File(localPathToFolder);
   }
   
   private String getCategory(File folderToCollect) {
      String category = folderToCollect.getParentFile().getName();
      if (category.equals("RECORDPOOL")) {
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
      }
      return category;
   }
   
   private void findAndDeleteCorruptedIn(File post) {
      File[] audioFiles = getAudioFiles(post);
      if (audioFiles != null && audioFiles.length > 0) {
         for (File aFile : audioFiles) {
            try {
               AudioFile audioFile = AudioFileIO.read(aFile);
            } catch (InvalidAudioFrameException | CannotReadException e) {
               logger.log("Corrupted file deleted: " + aFile.getName());
               aFile.delete();
            } catch (IOException | TagException | ReadOnlyFileException e) {
               logger.log("findAndDeleteCorruptedIn Exception: " + e);
            }
         }
      }
   }
   
   private File[] getFiles(File folderToCollect, String postCategory) {
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
   
   private void scheduleRelease(InfoForPost collectedInfo) {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MINUTE, ThreadLocalRandom.current().nextInt(480, 720));
      String scheduleTime = DATE_FORMAT.format(cal.getTime());
      Document scheduleTask = new Document()
        .append("releaseName", collectedInfo.getReleaseName())
        .append("scheduleTime", scheduleTime)
        .append("scheduleTimeMillis", cal.getTime().getTime());
      Log.write("Scheduling the Release: " + collectedInfo.getReleaseName()
        + " To Time: " + scheduleTime, "SCHEDULER");
      MONGO_CONTROL.scheduleCollection.insertOne(scheduleTask);
   }
   
   private File[] getAudioFiles(File post) {
      File folderToCollect = getFolderFromJson(post);
      String postCategory = getCategory(folderToCollect);
      return getFiles(folderToCollect, postCategory);
   }
   
   private void Sleep() {
      try {
         Thread.sleep(10 * 1000);
      } catch (InterruptedException ignored) {
      }
   }
   
}