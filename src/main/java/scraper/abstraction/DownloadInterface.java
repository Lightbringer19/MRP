package scraper.abstraction;

import lombok.Cleanup;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import utils.CheckDate;
import utils.CustomExecutor;
import utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static scheduler.ScheduleWatcher.addToScheduleDB;

@SuppressWarnings("ALL")
public interface DownloadInterface {
    
    default void downloadLinks(List<String> scrapedLinks, String releaseName) {
        getLogger().log("Downloading release: " + releaseName);
        String releaseFolderPath =
           "Z://TEMP FOR LATER/2019/" + CheckDate.getTodayDate() +
              "/RECORDPOOL/" + releaseName + "/";
        new File(releaseFolderPath).mkdirs();
        CustomExecutor downloadMaster = new CustomExecutor(15);
        scrapedLinks.stream()
           .map(downloadUrl -> new Thread(() ->
              downloadFile(downloadUrl, releaseFolderPath)))
           .forEach(downloadMaster::submit);
        downloadMaster.WaitUntilTheEnd();
        getLogger().log("Release Downloaded: " + releaseName);
        addToScheduleDB(new File(releaseFolderPath));
        getLogger().log("Release Scheduled: " + releaseName);
    }
    
    @SuppressWarnings("Duplicates")
    default void downloadFile(String url, String releaseFolderPath) {
        try {
            String downloadUrl = url.replaceAll(" ", "%20");
            @Cleanup CloseableHttpClient client = HttpClients.createDefault();
            HttpGet get = new HttpGet(downloadUrl);
            if (!url.contains("s3.amazonaws")) {
                get.setHeader("Cookie", getCookie());
            }
            get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
            @Cleanup CloseableHttpResponse response = client.execute(get);
            // System.out.println(downloadUrl + " " + response.getStatusLine().getStatusCode());
            String fileName;
            if (url.contains("headlinermusicclub") || url.contains("av.bpm")
               || url.contains("s3.amazonaws")) {
                String decode = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.name());
                fileName = URLDecoder.decode(
                   url.substring(url.lastIndexOf("/")), StandardCharsets.UTF_8.name())
                   .replace("?download", "");
                if (fileName.contains("?AWSAccessKeyId")) {
                    fileName = fileName.replace(
                       decode.substring(decode.indexOf("?AWSAccessKeyId")), "");
                }
            } else {
                fileName = response.getFirstHeader("Content-Disposition").getValue()
                   .replace("attachment; filename=", "")
                   .replace("attachment", "")
                   .replaceAll(";", "")
                   .replaceAll("\"", "")
                   .replaceAll("\\\\", "")
                   .replaceAll("&amp;", "&");
            }
            File mp3File = new File(releaseFolderPath + fileName);
            getLogger().log("Downloading: " + fileName + " | " + downloadUrl
               + " | " + response.getStatusLine());
            @Cleanup OutputStream outputStream = new FileOutputStream(mp3File);
            response.getEntity().writeTo(outputStream);
            getLogger().log("Downloaded: " + fileName);
        } catch (Exception e) {
            getLogger().log(e);
        }
    }
    
    String getCookie();
    
    Logger getLogger();
}
