package ftp.abstraction;

import configuration.YamlConfig;
import lombok.SneakyThrows;
import mongodb.MongoControl;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.bson.Document;
import utils.Logger;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.mongodb.client.model.Filters.eq;

public abstract class FtpManager extends Thread {
   
   protected static String SERVER;
   protected static String USERNAME;
   protected static String PASSWORD;
   protected static int PORT;
   
   protected static String CATEGORY_NAME;
   
   protected static MongoControl mongoControl;
   protected static YamlConfig.Config yamlConfig;
   
   protected static FTPClient ftpClient;
   
   protected static Logger logger;
   protected static String monthFolder;
   
   protected boolean longPeriod = false;
   
   public FtpManager() {
      mongoControl = new MongoControl();
      yamlConfig = new YamlConfig().config;
      ftpClient = new FTPClient();
   }
   
   @Override
   public void run() {
      Timer timer = new Timer();
      TimerTask ftpCheck = new TimerTask() {
         @Override
         @SneakyThrows
         public void run() {
            mainOperation();
         }
      };
      
      long sec = 1000;
      long min = 60 * sec;
      long hour = 60 * min;
      long period;
      
      if (longPeriod) {
         period = 2 * hour;
      } else {
         period = min;
      }
      
      timer.schedule(ftpCheck, 0, period);
   }
   
   public void mainOperation() {
      checkFtp();
   }
   
   public void checkFtp() {
      logger.log("Checking FTP for New Releases: " + CATEGORY_NAME);
      beforeCheck();
      try {
         ftpClient.connect(SERVER, PORT);
         ftpClient.login(USERNAME, PASSWORD);
         Log();
         ftpClient.enterLocalPassiveMode();
         ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
         
         FTPFile[] dayFtpFolders = ftpClient.listFiles(monthFolder);
         for (FTPFile dayFolder : dayFtpFolders) {
            String name = dayFolder.getName();
            logger.log("Checking day: " + name);
            
            if (!name.equals("_ALL_TRACKS__(ONLY_WORKS_WITH_FTP_CLIENT)")) {
               long time = dayFolder.getTimestamp().getTimeInMillis();
               //DB
               String dayReleasesPath = monthFolder + name;
               Document dayDoc = mongoControl.timeStampsCollection
                 .find(eq("folderPath", dayReleasesPath)).first();
               if (dayDoc == null) {
                  // DOWNLOAD
                  downloadNewReleases(dayReleasesPath);
                  //insert new DOC
                  mongoControl.timeStampsCollection
                    .insertOne(new Document("folderPath", dayReleasesPath)
                      .append("timeStamp", time));
               } else {
                  //check time: if time changed -> download
                  long timeStamp = (long) dayDoc.get("timeStamp");
                  if (time > timeStamp) {
                     downloadNewReleases(dayReleasesPath);
                  }
                  //update DB
                  dayDoc.put("timeStamp", time);
                  mongoControl.timeStampsCollection
                    .replaceOne(eq("_id",
                      dayDoc.getObjectId("_id")), dayDoc);
               }
            }
         }
      } catch (Exception e) {
         logger.log(e);
      } finally {
         try {
            ftpClient.logout();
            Log();
            ftpClient.disconnect();
         } catch (IOException e) {
            logger.log(e);
         }
      }
   }
   
   protected void beforeCheck() {
   }
   
   private void downloadNewReleases(String dayReleasesPath) throws IOException {
      logger.log("New Releases In Folder: " + dayReleasesPath);
      FTPFile[] releasesInDayFolder = ftpClient.listFiles(dayReleasesPath);
      for (FTPFile releaseFolder : releasesInDayFolder) {
         logger.log("Checking release: " + releaseFolder.getName());
         downloadRelease(dayReleasesPath, releaseFolder);
      }
   }
   
   protected abstract void downloadRelease(String dayReleasesPath, FTPFile releaseFolder) throws IOException;
   
   protected void Log() {
      System.out.print(ftpClient.getReplyString());
   }
   
}
