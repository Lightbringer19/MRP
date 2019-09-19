package ftp.djpool;

import ftp.abstraction.FtpManager;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPFile;
import org.bson.Document;
import utils.CheckDate;
import utils.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static scheduler.ScheduleWatcher.addToScheduleDB;

public class DJCFtp extends FtpManager {
   
   private List<String> SKIP;
   private Map<String, String> renameMap;
   
   public DJCFtp() {
      SERVER = yamlConfig.getRp_host();
      USERNAME = yamlConfig.getRp_username();
      PASSWORD = yamlConfig.getRp_password();
      PORT = 21;
      
      CATEGORY_NAME = "RECORDPOOL";
      logger = new Logger("DJ_POOL_FTP");
   
      longPeriod = true;
   }
   
   @Override
   @SneakyThrows
   public void mainOperation() {
      //this month check
      SimpleDateFormat formatter = new SimpleDateFormat("MM-MMM", Locale.US);
      String month = formatter
        .format(new Date()).toUpperCase();
      pathname = "/AUDIO/DATES/2019/" + month + "/";
      checkFtp();
      // past month check
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH, -1);
      String previousMonth = formatter
        .format(cal.getTime()).toUpperCase();
      pathname = "/AUDIO/DATES/2019/" + previousMonth + "/";
      checkFtp();
   }
   
   @Override
   @SuppressWarnings("unchecked")
   protected void beforeCheck() {
      SKIP = (List<String>) mongoControl
        .djc_skipCollection
        .find(eq("name", "skip"))
        .first().get("SKIP");
      renameMap = (Map<String, String>) mongoControl
        .djc_skipCollection
        .find(eq("name", "rename"))
        .first().get("rename");
   }
   
   @Override
   protected void downloadRelease(String dayReleasesPath, FTPFile releaseFolder) throws IOException {
      String releaseName = releaseFolder.getName();
      String releaseNameCleaned = cleanReleaseName(releaseName);
      String releaseRemotePath = dayReleasesPath + "/" + releaseName + "/";
      boolean download = toDownload(releaseName, releaseNameCleaned);
      if (download) {
         // download release
         logger.log("New Releases to Download: " + releaseNameCleaned);
         // create local release folder
         String releaseLocalPath =
           "Z:/TEMP FOR LATER/2019/" + CheckDate.getTodayDate()
             + "/" + CATEGORY_NAME + "/" + releaseNameCleaned;
         new File(releaseLocalPath).mkdirs();
         FTPFile[] releaseFiles = ftpClient.listFiles(releaseRemotePath);
         for (FTPFile releaseFile : releaseFiles) {
            if (!releaseFile.isFile()) {
               String subFolder = releaseRemotePath + releaseFile.getName() + "/";
               for (FTPFile file : ftpClient.listFiles(releaseRemotePath)) {
                  downloadFile(subFolder, releaseLocalPath, file);
               }
            } else {
               downloadFile(releaseRemotePath, releaseLocalPath, releaseFile);
            }
         }
         // ADD TO UPLOAD QUEUE
         logger.log("Release Downloaded: " + releaseNameCleaned);
         mongoControl.ftpDownloadedCollection
           .insertOne(new Document("releaseName", releaseNameCleaned));
         // ADD TO SCHEDULE
         addToScheduleDB(new File(releaseLocalPath));
      }
   }
   
   private void downloadFile(String releaseRemotePath, String releaseLocalPath, FTPFile releaseFile) throws IOException {
      try (OutputStream output = new FileOutputStream
        (releaseLocalPath + "/" + releaseFile.getName())) {
         ftpClient.retrieveFile(releaseRemotePath + releaseFile.getName(), output);
         Log();
         logger.log("File Downloaded: " + releaseFile.getName());
      } catch (FileNotFoundException e) {
         logger.log(e);
      }
   }
   
   private String cleanReleaseName(String releaseName) {
      for (Map.Entry<String, String> entry : renameMap.entrySet()) {
         releaseName = releaseName.replace(entry.getKey(), entry.getValue());
      }
      return releaseName;
   }
   
   private boolean toDownload(String releaseName, String releaseNameCleaned) {
      if (releaseName.equals(".") || releaseName.equals("..")) {
         return false;
      } else {
         for (String releaseNameToSkip : SKIP) {
            if (releaseName.toLowerCase().contains(releaseNameToSkip.toLowerCase())) {
               return false;
            }
         }
         return mongoControl.ftpDownloadedCollection
           .find(eq("releaseName", releaseNameCleaned))
           .first() == null;
      }
   }
}


