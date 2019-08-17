package ftp.djpool;

import ftp.FtpManager;
import org.apache.commons.net.ftp.FTPFile;
import org.bson.Document;
import utils.CheckDate;
import utils.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.mongodb.client.model.Filters.eq;
import static scheduler.ScheduleWatcher.addToScheduleDB;

public class DjPoolFtp extends FtpManager {
    
    public DjPoolFtp() {
        SERVER = yamlConfig.getRp_host();
        USERNAME = yamlConfig.getRp_username();
        PASSWORD = yamlConfig.getRp_password();
        PORT = 21;
        
        CATEGORY_NAME = "RECORDPOOL";
        logger = new Logger("DJ_POOL_FTP");
    
        longPeriod = true;
    }
    
    @Override
    protected void setPathName() {
        String month = new SimpleDateFormat("MM-MMM", Locale.US)
           .format(new Date()).toUpperCase();
        pathname = "/AUDIO/DATES/2019/" + month + "/";
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
            mongoControl.rpDownloadedCollection
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
        return releaseName
           .replace(" [DJC]", "")
           .replace("TrackPack", "MyRecordPool Pack")
           .replace("Dj City", "DJ City")
           .replace("DMS", "Direct Music Service")
           .replace("LoMaximo", "LoMaximoProductions");
    }
    
    private boolean toDownload(String releaseName, String releaseNameCleaned) {
        if (releaseName.equals(".") || releaseName.equals("..")) {
            return false;
        } else {
            String[] SKIP = {
               "BPM Supreme",
               "BPM Latino",
               "MP3 Pool Online",
               "8th Wonder",
               "Digital Music Pool",
               "Crate Connect",
               "BeatJunkies",
               "Headliner Music Pool",
               "MyMp3Pool",
               "HMC",
               "DMP",
               "ONLY_WORKS_WITH_FTP_CLIENT",
               "Ecuaremixes.com",
               "Latinremixes.com",
               "Pro Latin Remix",
               "Europa Remix",
               "Extreme Pro Remix",
               "IntesaMusic.net",
               "DVJ 3B",
               "DVJ Marcos Cabrera",
               "LatinoMusicPool",
               "LatinoMusicPool.com",
               "LMP",
               "DaZone",
               "Heavy Hits"
            };
            for (String releaseNameToSkip : SKIP) {
                if (releaseName.toLowerCase().contains(releaseNameToSkip.toLowerCase())) {
                    return false;
                }
            }
            return mongoControl.rpDownloadedCollection
               .find(eq("releaseName", releaseNameCleaned))
               .first() == null;
        }
    }
}


