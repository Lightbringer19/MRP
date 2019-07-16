package ftp.scene;

import configuration.YamlConfig;
import mongodb.MongoControl;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.bson.Document;
import utils.CheckDate;
import utils.Constants;
import utils.FUtils;
import utils.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.mongodb.client.model.Filters.eq;

public class SceneFTPManager {
    private static String SERVER_SCENE;
    private static String USERNAME_SCENE;
    private static String PASSWORD_SCENE;
    private static final int PORT_SCENE = 35695;

    private static FTPSClient ftpClient;
    private static MongoControl mongoControl = new MongoControl();
    private static String category;

    public SceneFTPManager() {
        YamlConfig yamlConfig = new YamlConfig();
        SERVER_SCENE = yamlConfig.config.getScene_host();
        USERNAME_SCENE = yamlConfig.config.getScene_username();
        PASSWORD_SCENE = yamlConfig.config.getScene_password();
    }

    public void checkFtp(String categoryToDownload) throws IOException {
        category = categoryToDownload;
        Log.write("Checking FTP for New Releases: " + category, "SCENE_FTP");
        ftpClient = new FTPSClient();
        ftpClient.connect(SERVER_SCENE, PORT_SCENE);
        ftpClient.login(USERNAME_SCENE, PASSWORD_SCENE);
        Log();
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        String ftpCategory = category.replace("SCENE-", "");
        String pathname = "/ENGLISH/" + ftpCategory + "/";

        FTPFile[] dayFtpFolders = ftpClient.listFiles(pathname);
        // 0707 - Example
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
                        .replaceOne(eq("_id", dayDoc.getObjectId("_id")), dayDoc);
            }
        }
        ftpClient.logout();
        Log();
        ftpClient.disconnect();
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
            Log.write("Release Downloaded: " + releaseName,
                    "SCENE_FTP");
            mongoControl.rpDownloadedCollection
                    .insertOne(new Document("releaseName", releaseName));
            // ADD TO UPLOAD QUEUE
            FUtils.writeFile(Constants.uploadDir, releaseName + ".json",
                    releaseLocalPath);
        }
    }

    private void downloadFile(String releaseRemotePath, String releaseLocalPath,
                              FTPFile releaseFile) throws IOException {
        OutputStream output = new FileOutputStream(
                releaseLocalPath + "/" + releaseFile.getName());
        //get the file from the remote system
        ftpClient.retrieveFile(releaseRemotePath
                + releaseFile.getName(), output);
        Log();
        //close output stream
        output.close();
        Log.write("File Downloaded: " + releaseFile.getName(), "SCENE_FTP");

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
