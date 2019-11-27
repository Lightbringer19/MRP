package boxcom;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.CheckDate;
import utils.Constants;
import utils.FUtils;
import utils.Log;

import java.io.File;

import static utils.Constants.uploadingDir;

public class Uploader extends Thread {
   static Scheduler FILE_UPLOAD_SCHEDULER =
     Schedulers.newParallel("Upload-File", 10);
   static Scheduler FOLDER_UPLOAD_SCHEDULER =
     Schedulers.newParallel("Upload-Folder", 9);
   
   public static void main(String[] args) {
      new Uploader().run();
   }
   
   private BoxCom boxControl;
   
   public Uploader() {
      boxControl = new BoxCom();
   }
   
   @SneakyThrows
   @Override
   public void run() {
      new File(Constants.uploadDir).mkdirs();
      do {
         File[] jsonFiles = new File(Constants.uploadDir).listFiles();
         if (jsonFiles != null && jsonFiles.length > 0) {
            Flux.just(jsonFiles)
              .parallel()
              .runOn(FOLDER_UPLOAD_SCHEDULER)
              .doOnNext(this::uploadRelease)
              .sequential()
              .blockLast();
         }
         Log.write(CheckDate.getNowTime() + " Uploader Sleeping",
           "Uploader");
         Thread.sleep(10000);
      } while (true);
   }
   
   private void uploadRelease(File infoJsonFile) {
      try {
         File folderToUpload = new File(FUtils.readFile(infoJsonFile));
         boxControl.UploadAndGetLink(folderToUpload, infoJsonFile);
         FileUtils.moveFileToDirectory(infoJsonFile,
           new File(uploadingDir), true);
      } catch (Exception e) {
         Log.write(e, "Uploader");
      }
   }
}
