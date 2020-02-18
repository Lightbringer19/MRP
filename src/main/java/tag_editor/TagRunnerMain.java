package tag_editor;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import utils.CheckDate;
import utils.Constants;
import utils.FUtils;

import java.io.File;

import static tag_editor.TagEditor.editMP3;
import static utils.Constants.filesDir;
import static utils.Constants.tagsDir;
import static utils.Log.write;

public class TagRunnerMain extends Thread {
   @SneakyThrows
   private static void DO() {
      write("Tagger Started", "Tagger");
      while (true) {
         try {
            // get info files
            File[] toTAG = new File(tagsDir).listFiles();
            for (File infoFile : toTAG) {
               File folderToEdit = new File(FUtils.readFile(infoFile));
               String category = folderToEdit.getParentFile().getName();
               if (category.contains("BEATPORT")) { // BEAT
                  BeatPortEdit(infoFile);
               } else if (category.contains("RECORDPOOL")) { // RecordPool
                  editRecordPool(infoFile, folderToEdit, category);
               }
            }
            write(CheckDate.getNowTime() + " Tagger Sleeping", "Tagger");
            sleep(10000);
         } catch (Exception e) {
            write(CheckDate.getNowTime() +
              " Tagger Sleeping With Exception " + e, "Tagger");
            write(e, "Tagger");
            sleep(5000);
         }
      }
   }
   
   @SneakyThrows
   private static void editRecordPool(File infoFile, File folderToEdit, String category) {
      // Edit tags in folder
      write("Editing Folder: " + folderToEdit.getName(), "Tagger");
      File[] RecordPoolFolder = folderToEdit.listFiles();
      if (RecordPoolFolder != null) {
         category = getSubCategory(category, RecordPoolFolder);
         if (category.equals("RECORDPOOL MUSIC")) {
            for (File file : folderToEdit.listFiles()) {
               if (file.getName().toLowerCase().endsWith(".mp3")) {
                  editMP3(file);
               }
            }
         } else if (category.equals("RECORDPOOL VIDEOS")) {
            for (File file : folderToEdit.listFiles()) {
               if (file.getName().toLowerCase().endsWith(".mp4")) {
                  TagEditor.EditMP4(file);
               }
            }
         } else {
            write("Folder Has No Mp3 or Mp4 files: " + folderToEdit.getName(), "Tagger");
            FileUtils.moveFileToDirectory(infoFile,
              new File(filesDir + "NOT-POSTED"), true);
            return;
         }
         write("Folder Edited: " + folderToEdit.getName(), "Tagger");
         FUtils.writeFile(Constants.uploadDir, folderToEdit.getName() + ".json",
           folderToEdit.getAbsolutePath());
      }
      infoFile.delete();
   }
   
   private static String getSubCategory(String category, File[] recordPoolFolder) {
      for (File RPfile : recordPoolFolder) {
         if (RPfile.getName().toLowerCase().endsWith(".mp3")) {
            category = "RECORDPOOL MUSIC";
            break;
         }
         if (RPfile.getName().toLowerCase().endsWith(".mp4")) {
            category = "RECORDPOOL VIDEOS";
            break;
         }
      }
      return category;
   }
   
   @SneakyThrows
   private static void BeatPortEdit(File infoFile) {
      try {
         // read path to TEMP_FOLDER
         File tempFolder = new File(FUtils.readFile(infoFile));
         File folderToTag = tempFolder;
         // rename folder
         folderToTag = renameFolder(folderToTag, " - electronicfresh.com");
         folderToTag = renameFolder(folderToTag, " electronicfresh.com");
         folderToTag = renameFolder(folderToTag, " - ElectronicFresh.com");
         folderToTag = renameFolder(folderToTag, "ElectronicFresh.com");
         folderToTag = renameFolder(folderToTag, "www.inevil.com");
         folderToTag = renameFolder(folderToTag, "inevil.com");
         // Edit tags in copied folder
         TagEditor.EditMP3TagsInFolder(folderToTag);
         infoFile.delete();
         // send info of edited folder to Uploader
         FUtils.writeFile(Constants.uploadDir,
           folderToTag.getName() + ".json", folderToTag.getAbsolutePath());
      } catch (Exception e) {
         write("Exception TagEdit " + e, "Tagger");
         sleep(5000);
      }
   }
   
   private static File renameFolder(File folderToTag, String stringToDelete) {
      String folderName = folderToTag.getName();
      if (folderName.contains(stringToDelete)) {
         folderName = folderName.replace(stringToDelete, "");
         File renamedFolder = new File(
           folderToTag.getParentFile().getAbsolutePath() + "//" + folderName);
         folderToTag.renameTo(renamedFolder);
         folderToTag = renamedFolder;
      }
      return folderToTag;
   }
   
   @Override
   public void run() {
      new File(tagsDir).mkdirs();
      try {
         sleep(500);
         DO();
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
   
}
