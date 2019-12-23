package poster;

import mongodb.MongoControl;
import utils.CheckDate;
import utils.Constants;
import utils.Log;

import java.io.File;

public class Poster extends Thread implements PosterInterface {
   public static MongoControl MONGO_CONTROL;
   
   @Override
   public void run() {
      Log.write(CheckDate.getNowTime() + " Poster Start", "Poster");
      MONGO_CONTROL = new MongoControl();
      new File(Constants.postDir).mkdirs();
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
            Log.write(e, "Poster");
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
