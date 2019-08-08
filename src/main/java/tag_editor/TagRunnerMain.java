package tag_editor;

import lombok.SneakyThrows;
import utils.CheckDate;
import utils.Constants;
import utils.FUtils;
import utils.Log;

import java.io.File;

import static tag_editor.TagEditor.editMP3;
import static utils.Constants.tagsDir;
import static wordpress.Poster.getRecordPoolCategory;

public class TagRunnerMain extends Thread {
    @SneakyThrows
    private static void DO() {
        Log.write("Tagger Started", "Tagger");
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
                Log.write(CheckDate.getNowTime() + " Tagger Sleeping", "Tagger");
                Thread.sleep(10000);
            } catch (Exception e) {
                Log.write(CheckDate.getNowTime() +
                   " Tagger Sleeping With Exception " + e, "Tagger");
                Log.write(e, "Tagger");
                Thread.sleep(5000);
            }
        }
    }
    
    private static void editRecordPool(File infoFile, File folderToEdit, String category) {
        // Edit tags in folder
        Log.write("Editing Folder: " + folderToEdit.getName(), "Tagger");
        category = getRecordPoolCategory(folderToEdit, category);
        if (category.equals("RECORDPOOL MUSIC")) {
            for (File file : folderToEdit.listFiles()) {
                if (file.getName().toLowerCase().endsWith(".mp3")) {
                    editMP3(file);
                }
            }
        } else {
            for (File file : folderToEdit.listFiles()) {
                if (file.getName().toLowerCase().endsWith(".mp4")) {
                    TagEditor.EditMP4(file);
                }
            }
        }
        infoFile.delete();
        Log.write("Folder Edited: " + folderToEdit.getName(), "Tagger");
        // send info of edited folder to Uploader
        FUtils.writeFile(Constants.uploadDir, folderToEdit.getName() + ".json",
           folderToEdit.getAbsolutePath());
    }
    
    @Override
    public void run() {
        new File(tagsDir).mkdirs();
        try {
            Thread.sleep(500);
            DO();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @SneakyThrows
    private static void BeatPortEdit(File infoFile) {
        try {
            // read path to TEMP_FOLDER
            File tempFolder = new File(FUtils.readFile(infoFile));
            File folderToTag = tempFolder;
            // rename folder
            folderToTag = renameFolder(folderToTag, " - electronicfresh.com");
            folderToTag = renameFolder(folderToTag, " - ElectronicFresh.com");
            // Edit tags in copied folder
            TagEditor.EditMP3TagsInFolder(folderToTag);
            infoFile.delete();
            // send info of edited folder to Uploader
            FUtils.writeFile(Constants.uploadDir,
               folderToTag.getName() + ".json", folderToTag.getAbsolutePath());
        } catch (Exception e) {
            Log.write("Exception TagEdit " + e, "Tagger");
            Thread.sleep(5000);
        }
    }
    
    private static File renameFolder(File folderToTag, String stringToReplace) {
        String folderName = folderToTag.getName();
        if (folderName.contains(stringToReplace)) {
            folderName = folderName.replace(stringToReplace, "");
            File renamedFolder = new File(
               folderToTag.getParentFile().getAbsolutePath() + "//" + folderName);
            folderToTag.renameTo(renamedFolder);
            folderToTag = renamedFolder;
        }
        return folderToTag;
    }
    
}
