package archiver;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import utils.CheckDate;
import utils.Constants;
import utils.FUtils;
import utils.Logger;

import java.io.File;

import static utils.Constants.archiveDir;

public class NasArchiver extends Thread {
   protected Logger logger = new Logger("NasArchiver");
   
   @Override
   @SneakyThrows
   public void run() {
      logger.log(CheckDate.getNowTime() + " NasArchiver Start");
      new File(archiveDir).mkdirs();
      while (true) {
         try {
            for (File archiveInfoFile : new File(Constants.archiveDir).listFiles()) {
               String sourcePath = FUtils.readFile(archiveInfoFile);
               File folderToArchive = new File(sourcePath);
               logger.log("Archiving Folder: " + folderToArchive.getName());
               FileUtils.moveDirectory(folderToArchive,
                 new File(sourcePath.replaceFirst("E", "Z")));
               logger.log("Folder Archived: " + folderToArchive.getName());
               archiveInfoFile.delete();
            }
            logger.log(CheckDate.getNowTime() + " NasArchiver SLEEPING");
            sleep(2000);
         } catch (Exception e) {
            logger.log(CheckDate.getNowTime() + " EXCEPTION " + e);
            sleep(2000);
         }
         
      }
   }
   
}
