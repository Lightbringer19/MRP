package boxcom;

import lombok.SneakyThrows;
import utils.CheckDate;
import utils.Constants;
import utils.FUtils;
import utils.Log;

import java.io.File;

public class Uploader extends Thread {
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
         // try {
         File[] TOUPLOAD = new File(Constants.uploadDir).listFiles();
         for (File infoJsonFile : TOUPLOAD) {
            File folderToUpload = new File(FUtils.readFile(infoJsonFile));
            boxControl.UploadAndGetLink(folderToUpload);
            infoJsonFile.delete();
         }
         Log.write(CheckDate.getNowTime() + " Uploader Sleeping",
           "Uploader");
         Thread.sleep(10000);
         // } catch (Exception e) {
         //    Log.write("EXCEPTION IN UPLOADER " + e, "Uploader");
         //    Log.write(e, "Uploader");
         //    Thread.sleep(5000);
         // }
      } while (true);
   }
}
