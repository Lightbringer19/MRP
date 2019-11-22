package wordpress;

import configuration.YamlConfig;
import mongodb.MongoControl;
import utils.CheckDate;
import utils.Constants;
import utils.Log;
import wordpress.utils.DownloadPoster;

import java.io.File;

public class Poster extends Thread implements PosterInterface {
   public static MongoControl MONGO_CONTROL;
   
   public static String MRP_AUTHORIZATION;
   public static DownloadPoster DOWNLOAD_POSTER;
   
   @Override
   public void run() {
      Log.write(CheckDate.getNowTime() + " Poster Start", "Poster");
      MONGO_CONTROL = new MongoControl();
      DOWNLOAD_POSTER = new DownloadPoster();
      new File(Constants.postDir).mkdirs();
      YamlConfig yamlConfig = new YamlConfig();
      MRP_AUTHORIZATION = yamlConfig.config.getMrp_authorization();
      while (true) {
         try {
            for (File categoryFolder : new File(Constants.postDir).listFiles()) {
               for (File postFile : categoryFolder.listFiles()) {
                  String category = postFile.getParentFile().getName();
                  Log.write("Preparing to post: " + postFile.getName()
                    + "| In: " + category, "Poster");
                  post(postFile.getName());
                  postFile.delete();
                  Thread.sleep(2000);
               }
            }
            Log.write(CheckDate.getNowTime() + " POSTER SLEEPING", "Poster");
            Sleep();
         } catch (Exception e) {
            Log.write(CheckDate.getNowTime() + " EXCEPTION " + e, "Poster");
            Sleep();
         }
      }
   }
   
   private static void Sleep() {
      try {
         Thread.sleep(10 * 1000);
      } catch (InterruptedException ignored) {
      }
   }
}
