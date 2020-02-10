package ftp.djpool;

import ftp.abstraction.FtpManager;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPFile;
import org.bson.Document;
import utils.CheckDate;
import utils.FUtils;
import utils.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static utils.CheckDate.getCurrentYear;
import static utils.Constants.tagsDir;

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
      monthFolder = "/AUDIO/DATES/" + getCurrentYear() + "/" + month + "/";
      checkFtp();
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH, -1);
      String previousMonth = formatter
        .format(cal.getTime()).toUpperCase();
      String year = new SimpleDateFormat("yyyy").format(cal.getTime());
      monthFolder = "/AUDIO/DATES/" + year + "/" + previousMonth + "/";
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
      if (toDownload(releaseName)) {
         String releaseNameCleaned = cleanReleaseName(releaseName);
         String releaseRemotePath = dayReleasesPath + "/" + releaseName + "/";
         // download release
         logger.log("New Releases to Download: " + releaseNameCleaned);
         // create local release folder
         String releaseLocalPath =
           "E:/TEMP FOR LATER/" + getCurrentYear() + "/" + CheckDate.getTodayDate()
             + "/" + CATEGORY_NAME + "/" + releaseNameCleaned;
         FTPFile[] releaseFiles = ftpClient.listFiles(releaseRemotePath);
         boolean noSubFolders = true;
         for (FTPFile releaseFile : releaseFiles) {
            if (!releaseFile.isFile()) {
               String subfolderName = releaseFile.getName();
               String subfolderReleaseName = releaseNameCleaned + " " + subfolderName;
               String subfolderFtpPath = releaseRemotePath + subfolderName + "/";
               String subFolderLP = releaseLocalPath + " " + subfolderName;
               for (FTPFile file : ftpClient.listFiles(subfolderFtpPath)) {
                  downloadFile(subfolderFtpPath, subFolderLP, file);
               }
               logger.log("Subfolder Downloaded: " + subfolderReleaseName);
               FUtils.writeFile(tagsDir, subfolderReleaseName + ".json", subFolderLP);
               noSubFolders = false;
            } else {
               downloadFile(releaseRemotePath, releaseLocalPath, releaseFile);
            }
         }
         // ADD TO QUEUE
         logger.log("Release Downloaded: " + releaseNameCleaned);
         mongoControl.djcDownloadedCollection
           .insertOne(new Document("releaseName", releaseNameCleaned)
             .append("date", new Date()));
         // ADD TO SCHEDULE
         if (noSubFolders) {
            FUtils.writeFile(tagsDir, releaseNameCleaned + ".json", releaseLocalPath);
         }
      }
   }
   
   private void downloadFile(String releaseRemotePath, String releaseLocalPath, FTPFile releaseFile) throws IOException {
      new File(releaseLocalPath).mkdirs();
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
   
   private boolean toDownload(String releaseName) {
      if (releaseName.equals(".") || releaseName.equals("..")) {
         return false;
      } else {
         for (String releaseNameToSkip : SKIP) {
            if (releaseName.toLowerCase().contains(releaseNameToSkip.toLowerCase())) {
               return false;
            }
         }
         return mongoControl.djcDownloadedCollection
           .find(eq("releaseName", cleanReleaseName(releaseName)))
           .first() == null;
      }
   }
}


