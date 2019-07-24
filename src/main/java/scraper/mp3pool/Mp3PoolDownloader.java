package scraper.mp3pool;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import utils.CustomExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import static scraper.mp3pool.Mp3poolDriver.cookieForAPI;
import static scraper.mp3pool.Mp3poolDriver.mp3Logger;

class Mp3PoolDownloader {

    @SuppressWarnings("Duplicates")
    static void downloadLinks(List<String> scrapedLinks, String dateToDownload,
                              String releaseFolderPath) {
        mp3Logger.log("Downloading release from date: " + dateToDownload);
        new File(releaseFolderPath).mkdirs();
        CustomExecutor downloadMaster = new CustomExecutor(15);
        scrapedLinks.stream()
                .map(downloadUrl -> new Thread(() ->
                        downloadFile(downloadUrl, releaseFolderPath)))
                .forEach(downloadMaster::submit);
        downloadMaster.WaitUntilTheEnd();
    }

    @SuppressWarnings("Duplicates")
    @SneakyThrows
    private static void downloadFile(String url, String releaseFolderPath) {
        @Cleanup CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        get.setHeader("Cookie", cookieForAPI);
        get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
        @Cleanup CloseableHttpResponse response = client.execute(get);
        String fileName = response.getFirstHeader("Content-Disposition").getValue()
                .replace("attachment; filename=", "")
                .replaceAll("\"", "");
        File mp3File = new File(releaseFolderPath + fileName);
        mp3Logger.log("Downloading file: " + fileName + " " + url
                + " | " + response.getStatusLine());
        @Cleanup OutputStream outputStream = new FileOutputStream(mp3File);
        response.getEntity().writeTo(outputStream);
    }

}
