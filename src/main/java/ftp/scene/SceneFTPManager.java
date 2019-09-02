package ftp.scene;

import configuration.YamlConfig;
import lombok.SneakyThrows;
import mongodb.MongoControl;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.bson.Document;
import utils.CheckDate;
import utils.Constants;
import utils.FUtils;
import utils.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.mongodb.client.model.Filters.eq;

public class SceneFTPManager extends Thread {
   private static final int PORT_SCENE = 35695;
   private static String SERVER_SCENE;
   private static String USERNAME_SCENE;
   private static String PASSWORD_SCENE;
   private static FTPSClient ftpClient = new FTPSClient();
   private static MongoControl mongoControl = new MongoControl();
   private static String category;
   
   public SceneFTPManager() {
      YamlConfig yamlConfig = new YamlConfig();
      SERVER_SCENE = yamlConfig.config.getScene_host();
      USERNAME_SCENE = yamlConfig.config.getScene_username();
      PASSWORD_SCENE = yamlConfig.config.getScene_password();
   }
   
   @Override
   public void run() {
      SceneFTPManager sceneFTPManager = new SceneFTPManager();
      Timer timer = new Timer();
      TimerTask ftpCheckMp3 = new TimerTask() {
         @Override
         @SneakyThrows
         public void run() {
            sceneFTPManager.checkFtp("SCENE-MP3");
         }
      };
      TimerTask ftpCheckFlac = new TimerTask() {
         @Override
         @SneakyThrows
         public void run() {
            sceneFTPManager.checkFtp("SCENE-FLAC");
         }
      };
      long sec = 1000;
      long min = sec * 60;
      timer.schedule(ftpCheckMp3, 0, min);
      timer.schedule(ftpCheckFlac, 0, min);
   }
   
   private void checkFtp(String categoryToDownload) {
      category = categoryToDownload;
      Log.write("Checking FTP for New Releases: " + category, "SCENE_FTP");
      try {
         ftpClient.connect(SERVER_SCENE, PORT_SCENE);
         ftpClient.login(USERNAME_SCENE, PASSWORD_SCENE);
         Log();
         ftpClient.enterLocalPassiveMode();
         ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
         
         String ftpCategory = category.replace("SCENE-", "");
         String pathname = "/ENGLISH/" + ftpCategory + "/";
         
         FTPFile[] dayFtpFolders = ftpClient.listFiles(pathname);
         for (FTPFile dayFolder : dayFtpFolders) {
            String name = dayFolder.getName();
            long time = dayFolder.getTimestamp().getTimeInMillis();
            //DB
            String dayReleasesPath = pathname + name;
            Document dayDoc = mongoControl.dayFolderTimeCollection
              .find(eq("folderPath", dayReleasesPath)).first();
            if (dayDoc == null) {
               //insert new DOC
               mongoControl.dayFolderTimeCollection
                 .insertOne(new Document("folderPath", dayReleasesPath)
                   .append("timeStamp", time));
               // DOWNLOAD
               downloadNewReleases(dayReleasesPath);
            } else {
               //check time: if time changed -> download
               long timeStamp = (long) dayDoc.get("timeStamp");
               if (time > timeStamp) {
                  downloadNewReleases(dayReleasesPath);
               }
               //update DB
               dayDoc.put("timeStamp", time);
               mongoControl.dayFolderTimeCollection
                 .replaceOne(eq("_id",
                   dayDoc.getObjectId("_id")), dayDoc);
            }
         }
      } catch (Exception e) {
         Log.write(e, "SCENE_FTP");
      } finally {
         try {
            ftpClient.logout();
            Log();
            ftpClient.disconnect();
         } catch (IOException e) {
            Log.write(e, "SCENE_FTP");
         }
      }
   }
   
   private void downloadNewReleases(String dayReleasesPath) throws IOException {
      //download new Releases in folder
      Log.write("New Releases In Folder: " + dayReleasesPath,
        "SCENE_FTP");
      FTPFile[] releasesInDayFolder = ftpClient.listFiles(dayReleasesPath);
      // 0707/releaseName - Example
      for (FTPFile releaseFolder : releasesInDayFolder) {
         downloadRelease(dayReleasesPath, releaseFolder);
      }
   }
   
   private void downloadRelease(String dayReleasesPath, FTPFile releaseFolder)
     throws IOException {
      String releaseName = releaseFolder.getName();
      String releaseRemotePath = dayReleasesPath + "/" + releaseName + "/";
      //Skip check
      boolean download = toDownload(releaseName);
      if (download) {
         // download release
         Log.write("New Release to Download: " + releaseName,
           "SCENE_FTP");
         // create local release folder
         String releaseLocalPath =
           "Z:/TEMP FOR LATER/2019/" + CheckDate.getTodayDate()
             + "/" + category + "/" + releaseName;
         new File(releaseLocalPath).mkdirs();
         // .mp3 or .flac or any other files in the release folder
         FTPFile[] releaseFiles = ftpClient.listFiles(releaseRemotePath);
         for (FTPFile releaseFile : releaseFiles) {
            if (!releaseFile.isFile()) {
               continue;
            }
            downloadFile(releaseRemotePath, releaseLocalPath, releaseFile);
         }
         // DELETE -missing files
         Arrays.stream(Objects.requireNonNull(new File(releaseLocalPath).listFiles()))
           .forEach(file -> {
              if (file.getName().contains("-missing")) {
                 file.delete();
              }
           });
         // ADD TO UPLOAD QUEUE
         Log.write("Release Downloaded: " + releaseName,
           "SCENE_FTP");
         mongoControl.rpDownloadedCollection
           .insertOne(new Document("releaseName", releaseName));
         FUtils.writeFile(Constants.uploadDir, releaseName + ".json",
           releaseLocalPath);
      }
   }
   
   private void downloadFile(String releaseRemotePath, String releaseLocalPath,
                             FTPFile releaseFile) throws IOException {
      try (OutputStream output = new FileOutputStream(
        releaseLocalPath + "/" + releaseFile.getName())) {
         ftpClient.retrieveFile(releaseRemotePath
           + releaseFile.getName(), output);
         Log();
         Log.write("File Downloaded: " + releaseFile.getName(),
           "SCENE_FTP");
      } catch (FileNotFoundException e) {
         Log.write(e, "SCENE_FTP");
      }
      
   }
   
   private boolean toDownload(String releaseName) {
      if (releaseName.contains("(incomplete)")) {
         return false;
      } else {
         // check if release downloaded
         return mongoControl.rpDownloadedCollection
           .find(eq("releaseName", releaseName))
           .first() == null;
      }
   }
   
   private void Log() {
      System.out.print(ftpClient.getReplyString());
   }
}
