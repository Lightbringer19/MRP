package scraper.abstraction;

import lombok.Cleanup;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.FUtils;
import utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.CheckDate.getCurrentYear;
import static utils.CheckDate.getTodayDate;
import static utils.Constants.tagsDir;

@SuppressWarnings("ALL")
public interface DownloadInterface {
   
   default void downloadLinks(List<String> scrapedLinks, String releaseName) {
      getLogger().log("Downloading release: " + releaseName);
      String releaseFolderPath =
        "E://TEMP FOR LATER/" + getCurrentYear() + "/" + getTodayDate() +
          "/RECORDPOOL/" + releaseName + "/";
      new File(releaseFolderPath).mkdirs();
      Scheduler scheduler = Schedulers.newParallel("Download", 15);
      Flux.fromIterable(scrapedLinks)
        .parallel()
        .runOn(scheduler)
        .doOnNext(downloadUrl -> downloadFile(downloadUrl, releaseFolderPath))
        .sequential()
        .blockLast();
      scheduler.dispose();
      getLogger().log("Release Downloaded: " + releaseName);
      FUtils.writeFile(tagsDir.replace("\\Scrapers", ""), releaseName + ".json",
        releaseFolderPath);
      getLogger().log("Release added to tag queue: " + releaseName);
   }
   
   @SuppressWarnings("Duplicates")
   default void downloadFile(String url, String releaseFolderPath) {
      try {
         String downloadUrl = url.replaceAll(" ", "%20");
         @Cleanup CloseableHttpClient client = HttpClients.createDefault();
         HttpGet get = new HttpGet(downloadUrl);
         get.setHeader("Cookie", getCookie());
         get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
         @Cleanup CloseableHttpResponse response = client.execute(get);
         String fileName = "";
         if (response.getFirstHeader("Content-Disposition") != null) {
            String content = response.getFirstHeader("Content-Disposition").getValue();
            if (content.contains("attachment; filename=")) {
               int index = content.indexOf("\"");
               fileName = content
                 .substring(index + 1, content.indexOf("\"", index + 1));
               fileName = decode(fileName, UTF_8.name())
                 .replace("attachment; filename=", "")
                 .replace("attachment", "")
                 .replace("attachement", "")
                 .replaceAll(";", "")
                 .replaceAll("\"", "")
                 .replaceAll("\\\\", "")
                 .replaceAll("&amp;", "&");
            }
         }
         if (fileName.equals("")) {
            String decode = decode(url, UTF_8.name());
            fileName = decode(
              url.substring(url.lastIndexOf("/")), UTF_8.name());
            if (fileName.contains("?")) {
               fileName = fileName.replace(
                 decode.substring(decode.indexOf("?")), "");
            }
         }
         fileName = fileName.replaceAll("/", "");
         String pathname = releaseFolderPath + "/" + fileName;
         File mp3File = new File(pathname);
         getLogger().log("Downloading: " + fileName + " | " + downloadUrl
           + " | " + response.getStatusLine());
         if (response.getStatusLine().getStatusCode() != 403) {
            @Cleanup OutputStream outputStream = new FileOutputStream(mp3File);
            response.getEntity().writeTo(outputStream);
            getLogger().log("Downloaded: " + fileName);
         } else {
            getLogger().log("Not Downloaded: " + fileName);
         }
      } catch (Exception e) {
         getLogger().log(e);
      }
   }
   
   default String getLocation(String url) {
      try {
         @Cleanup CloseableHttpClient client = HttpClients.createDefault();
         HttpGet get = new HttpGet(url);
         RequestConfig.Builder builder = RequestConfig.custom();
         RequestConfig requestConfig = builder.setRedirectsEnabled(false).build();
         get.setConfig(requestConfig);
         get.setHeader("Cookie", getCookie());
         get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
         @Cleanup CloseableHttpResponse response = client.execute(get);
         String location = response.getFirstHeader("Location").getValue();
         System.out.println(url + " | " + location + " | " + response.getStatusLine().getStatusCode());
         return location.replaceAll(" ", "%20");
      } catch (Exception e) {
         getLogger().log(e);
      }
      return null;
   }
   
   String getCookie();
   
   Logger getLogger();
}
