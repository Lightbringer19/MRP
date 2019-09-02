package utils;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class FUtils {
   
   public static String readFile(File file) {
      try {
         return FileUtils.readFileToString(file, "UTF-8");
      } catch (Exception e) {
         Log.write("readFile " + e, "Exception");
      }
      return null;
   }
   
   public static void writeFile(String folderPath, String fileName, String fileText) {
      try {
         new File(folderPath).mkdirs();
         File file = new File(folderPath + "//" + fileName);
         FileUtils.writeStringToFile(file, fileText, "UTF-8");
      } catch (Exception e) {
         Log.write("writeFile " + e, "Exception");
      }
   }
   
   public static void writeToFile(String releaseName, String link, String categoryName) {
      writeCrawlScript(releaseName, link, categoryName);
      // Log.write("CrawlScript created: " + releaseName, "Scraper");
   }
   
   public static void writeToFile(String releaseName, String link) {
      String categoryName = "RECORDPOOL";
      writeCrawlScript(releaseName, link, categoryName);
   }
   
   public static void writeCrawlScript(String releaseName, String link, String categoryName) {
      String path = "Z:\\TEMP FOR LATER\\2019\\" + CheckDate.getTodayDate() + "\\" + categoryName + "\\<jd:packagename>";
      String crawlScript = "text=" + link +
        "\r\n" +
        "downloadFolder=" + path +
        "\r\n" +
        "packageName=" + releaseName +
        "\r\n";
      
      String finalFolder = "C:\\Users\\mhnyc\\AppData\\Local\\JDownloader 2.0\\folderwatch";
      // String finalFolder = "D:\\Program Files (x86)\\JDownloader\\folderwatch";
      String fileName = releaseName + ".crawljob";
      writeFile(finalFolder, fileName, crawlScript);
      // Log.write("CrawlScript created: " + releaseName, "Scraper");
   }
}
