package ftp.beatport;

import ftp.abstraction.FtpManager;
import org.apache.commons.net.ftp.FTPFile;
import org.bson.Document;
import utils.Constants;
import utils.FUtils;
import utils.Logger;

import java.io.*;

import static com.mongodb.client.model.Filters.eq;
import static utils.CheckDate.getCurrentYear;
import static utils.CheckDate.getTodayDate;

public class BeatportFtp extends FtpManager {
   
   public BeatportFtp() {
      SERVER = yamlConfig.getBeat_host();
      USERNAME = yamlConfig.getBeat_username();
      PASSWORD = yamlConfig.getBeat_password();
      PORT = 7777;
      
      CATEGORY_NAME = "BEATPORT";
      monthFolder = "/MP3/BEATPORT__AND__WEBSITE_SECTION/";
      logger = new Logger("Beatport_FTP");
   }
   
   @Override
   protected void downloadRelease(String dayReleasesPath, FTPFile releaseFolder) throws IOException {
      String releaseName = releaseFolder.getName();
      String releaseRemotePath = dayReleasesPath + "/" + releaseName + "/";
      boolean download = toDownload(releaseName);
      if (download) {
         // download release
         logger.log("New Releases to Download: " + releaseName);
         // create local release folder
         String releaseLocalPath =
           "E:/TEMP FOR LATER/" + getCurrentYear() + "/" + getTodayDate()
             + "/" + CATEGORY_NAME + "/" + releaseName;
         new File(releaseLocalPath).mkdirs();
         FTPFile[] releaseFiles = ftpClient.listFiles(releaseRemotePath);
         for (FTPFile releaseFile : releaseFiles) {
            if (!releaseFile.isFile()) {
               continue;
            }
            downloadFile(releaseRemotePath, releaseLocalPath, releaseFile);
            
         }
         // ADD TO UPLOAD QUEUE
         logger.log("Release Downloaded: " + releaseName);
         mongoControl.ftpDownloadedCollection
           .insertOne(new Document("releaseName", releaseName));
         FUtils.writeFile(Constants.tagsDir, releaseName + ".json",
           releaseLocalPath);
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
   
   private boolean toDownload(String releaseName) {
      if (releaseName.equals(".") || releaseName.equals("..")) {
         return false;
      } else {
         // check if release downloaded
         return mongoControl.ftpDownloadedCollection
           .find(eq("releaseName", releaseName))
           .first() == null;
      }
   }
}
