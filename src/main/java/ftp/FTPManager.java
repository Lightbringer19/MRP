package ftp;

import configuration.YamlConfig;
import mongodb.MongoControl;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.bson.Document;
import utils.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.mongodb.client.model.Filters.eq;
import static utils.FUtils.writeToFile;

public class FTPManager {

    private static String SERVER_RP;
    private static String USERNAME_RP;
    private static String PASSWORD_RP;
    private static int PORT_RP = 21;

    private static String SERVER_BEATPORT;
    private static String USERNAME_BEATPORT;
    private static String PASSWORD_BEATPORT;
    private static final int PORT_BEATPORT = 7777;

    private static String SERVER;
    private static String USERNAME;
    private static String PASSWORD;
    private static int PORT;

    private static String CATEGORY_NAME;

    // private static final String SERVER = "127.0.0.1";
    // private static final String USERNAME = "Admin";
    // private static final String PASSWORD = "1234";

    private static final String[] SKIP = {
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
            "ONLY_WORKS_WITH_FTP_CLIENT"
    };

    private MongoControl mongoControl = new MongoControl();
    private FTPClient ftpClient = new FTPClient();

    FTPManager() {
        YamlConfig yamlConfig = new YamlConfig();
        SERVER_RP = yamlConfig.config.getRp_host();
        USERNAME_RP = yamlConfig.config.getRp_username();
        PASSWORD_RP = yamlConfig.config.getRp_password();
        SERVER_BEATPORT = yamlConfig.config.getBeat_host();
        USERNAME_BEATPORT = yamlConfig.config.getBeat_username();
        PASSWORD_BEATPORT = yamlConfig.config.getBeat_password();
    }

    void checkFtp(String categoryName) throws IOException {
        CATEGORY_NAME = categoryName;
        Log.write("Checking FTP for New Releases: " + CATEGORY_NAME, "FTP&SCHEDULER");
        String pathname;
        if (categoryName.equals("BEATPORT")) {
            SERVER = SERVER_BEATPORT;
            USERNAME = USERNAME_BEATPORT;
            PASSWORD = PASSWORD_BEATPORT;
            PORT = PORT_BEATPORT;
            pathname = "/MP3/BEATPORT__AND__WEBSITE_SECTION/";
        } else {
            SERVER = SERVER_RP;
            USERNAME = USERNAME_RP;
            PASSWORD = PASSWORD_RP;
            PORT = PORT_RP;
            String month = new SimpleDateFormat("MM-MMM", Locale.US)
                    .format(new Date()).toUpperCase();
            pathname = "/AUDIO/DATES/2019/" + month + "/";
        }

        ftpClient.connect(SERVER, PORT);
        ftpClient.login(USERNAME, PASSWORD);
        Log();
        ftpClient.enterLocalPassiveMode();

        FTPFile[] dayFtpFolders = ftpClient.listFiles(pathname);
        for (FTPFile ftpFile : dayFtpFolders) {
            String name = ftpFile.getName();
            if (!name.equals("_ALL_TRACKS__(ONLY_WORKS_WITH_FTP_CLIENT)")) {
                long time = ftpFile.getTimestamp().getTimeInMillis();
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
                    //update
                    dayDoc.put("timeStamp", time);
                    mongoControl.dayFolderTimeCollection
                            .replaceOne(eq("_id", dayDoc.getObjectId("_id")), dayDoc);
                }
            }
        }
        ftpClient.disconnect();
    }

    private void downloadNewReleases(String dayReleasesPath) throws IOException {
        //download new Releases in folder
        Log.write("New Releases In Folder: " + dayReleasesPath,
                "FTP&SCHEDULER");
        FTPFile[] releasesInDayFolder = ftpClient.listFiles(dayReleasesPath);
        for (FTPFile releaseFolder : releasesInDayFolder) {
            String releaseName = releaseFolder.getName();
            String releaseNameCleaned = releaseName
                    .replace(" [DJC]", "")
                    .replace("TrackPack", "MyRecordPool Pack");
            String releasePath = dayReleasesPath + "/" + releaseName;
            //  Skip releases from sheet
            boolean download = toDownload(releaseName, releaseNameCleaned);
            if (download) {
                // download release
                Log.write("New Releases to Download: " + releaseName,
                        "FTP&SCHEDULER");
                // check for sub folders
                FTPFile[] releaseFiles = ftpClient.listFiles(releasePath);
                String link;
                if (releaseFiles[0].isDirectory()) {
                    // it's a folder with sub folders
                    List<String> subLinks = new ArrayList<>();
                    for (FTPFile releaseFile : releaseFiles) {
                        String subName = releasePath + "/" + releaseFile.getName();
                        String subLink =
                                "ftp://" + USERNAME.replace("@", "%40")
                                        + ":" + PASSWORD + "@" + SERVER + ":" + PORT +
                                        subName.replaceAll(" ", "%20");
                        subLinks.add(subLink);
                    }
                    link = String.join(" ", subLinks);
                } else {
                    link = "ftp://" + USERNAME.replace("@", "%40")
                            + ":" + PASSWORD + "@" +
                            SERVER + ":" + PORT + releasePath;
                }
                writeToFile(releaseNameCleaned, link, CATEGORY_NAME);
                // add to TO_DOWNLOAD QUEUE
                mongoControl.toDownloadCollection.insertOne(
                        new Document("releaseName", releaseNameCleaned)
                                .append("link", link)
                                .append("categoryName", CATEGORY_NAME));
            }
        }
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
            // check if release downloaded
            boolean releaseNotDownloaded = mongoControl.rpDownloadedCollection
                    .find(eq("releaseName", releaseNameCleaned))
                    .first() == null;
            // CHECK IF RELEASE IS IN QUEUE TO DOWNLOAD
            boolean releaseNotInDownloadQueue = mongoControl.toDownloadCollection
                    .find(eq("releaseName", releaseNameCleaned))
                    .first() == null;
            return releaseNotDownloaded && releaseNotInDownloadQueue;
        }
    }

    private void Log() {
        System.out.print(ftpClient.getReplyString());
    }
}
