package boxcom;

import com.box.sdk.*;
import com.box.sdk.BoxFolder.Info;
import com.google.gson.Gson;
import configuration.YamlConfig;
import json.InfoFromBoxCom;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.Constants;
import utils.FUtils;
import utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import static com.box.sdk.BoxSharedLink.Access.OPEN;

class BoxCom extends Thread {
   private static final String CONFIG = Constants.filesDir + "config.json";
   
   private static final String ID_2019 = "62866040452";
   private static final String TEST_FOLDER_ID = "77312192064";
   private static String USER_ID;
   private static BoxFolder FOLDER_2019;
   private static BoxFolder TEST_FOLDER;
   
   private static BoxSharedLink.Permissions permissions;
   private static final Scheduler SCHEDULER = Schedulers.newParallel("Upload", 10);
   
   //add security.provider.11=org.bouncycastle.jce.provider.BouncyCastleProvider
   //to java.security in lib/security
   // and add bc provider to lib/ext
   
   BoxCom() {
      // FOLDER_2019 = new BoxFolder(GetClient(), TEST_FOLDER_ID);
      permissions = new BoxSharedLink.Permissions();
      YamlConfig yamlConfig = new YamlConfig();
      USER_ID = yamlConfig.config.getBox_user_id();
      FOLDER_2019 = new BoxFolder(GetClient(), ID_2019);
      permissions.setCanDownload(true);
      permissions.setCanPreview(true);
   }
   
   private static BoxDeveloperEditionAPIConnection GetClient() {
      try {
         FileReader reader = new FileReader(CONFIG);
         BoxConfig boxConfig = BoxConfig.readFrom(reader);
         int MAX_CACHE_ENTRIES = 100;
         IAccessTokenCache accessTokenCache =
           new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);
         return BoxDeveloperEditionAPIConnection
           .getAppUserConnection(USER_ID, boxConfig, accessTokenCache);
      } catch (Exception e) {
         Log.write("Exception in GetClient: " + e, "Uploader");
      }
      return null;
   }
   
   @SneakyThrows
   private static BoxFolder searchForFolder(BoxFolder parentFolder, String search) {
      do {
         for (BoxItem.Info itemInfo : parentFolder) {
            if (itemInfo instanceof Info) {
               Info folderInfo = (Info) itemInfo;
               String name = folderInfo.getName();
               if (name.equals(search)) {
                  return folderInfo.getResource();
               }
            }
         }
         try {
            // if folder not found create new one
            BoxFolder.Info newFolder = parentFolder.createFolder(search);
            return newFolder.getResource();
         } catch (BoxAPIResponseException e) {
            Log.write("Folder is here ", "Uploader");
            Log.write(e, "Uploader");
            sleep(5000);
         }
      } while (true);
   }
   
   void UploadAndGetLink(File folderToUpload) {
      try {
         BoxFolder dayFolder = searchForFolder(FOLDER_2019,
           folderToUpload.getParentFile().getParentFile().getName());
         BoxFolder categoryFolder =
           searchForFolder(dayFolder, folderToUpload.getParentFile().getName());
         Log.write("Uploading This Folder: " + folderToUpload.getName(),
           "Uploader");
         BoxFolder newFolder = searchForFolder(categoryFolder, folderToUpload.getName());
         Flux.just(folderToUpload.listFiles())
           .parallel()
           .runOn(SCHEDULER)
           .doOnNext(file -> UploadFile(newFolder, file))
           .sequential()
           .blockLast();
         getLinkAndName(newFolder, folderToUpload);
      } catch (Exception e) {
         Log.write("Exception in UploadAndGetLink: " + e, "Uploader");
         Log.write(e, "Uploader");
      }
   }
   
   @SneakyThrows
   private void UploadFile(BoxFolder folder, File fileToUpload) {
      while (true) {
         Log.write("Uploading File: " + fileToUpload.getName(), "Uploader");
         try (FileInputStream stream = new FileInputStream(fileToUpload)) {
            BoxFile.Info newFileInfo = folder.uploadFile(stream, fileToUpload.getName());
            Log.write("File Uploaded: " + fileToUpload.getName(), "Uploader");
            break;
         } catch (Exception e) {
            Log.write(e, "Uploader");
            sleep(2000);
         }
      }
   }
   
   private void getLinkAndName(BoxFolder folder, File folderToUpload) {
      BoxSharedLink sharedLink = folder.createSharedLink(OPEN, null, permissions);
      
      Info folderInfo = folder.getInfo();
      String folderName = folderInfo.getName().replaceAll("_", " ");
      String link = sharedLink.getURL();
      
      InfoFromBoxCom infoFromBoxCom = new InfoFromBoxCom();
      infoFromBoxCom.setDownloadLinkBoxCom(link);
      infoFromBoxCom.setReleaseName(folderName);
      infoFromBoxCom.setLocalPathToFolder(folderToUpload.getAbsolutePath());
      
      Log.write("Info for Post: " + (folderName + " | " + link), "Uploader");
      FUtils.writeFile(Constants.collectorDir + folderInfo.getParent().getName(),
        folderName + ".json", new Gson().toJson(infoFromBoxCom));
   }
}
