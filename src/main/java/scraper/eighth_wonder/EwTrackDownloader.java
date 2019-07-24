package scraper.eighth_wonder;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import utils.CheckDate;
import utils.CustomExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static ftp.ScheduleWatcher.addToScheduleDB;
import static scraper.eighth_wonder.EwDriver.cookieForAPI;
import static scraper.eighth_wonder.EwDriver.ewLogger;

class EwTrackDownloader {

    @SuppressWarnings("Duplicates")
    static void downloadLinks(List<String> scrapedLinks, String dateToDownload, String category)
            throws ParseException {
        ewLogger.log("Downloading release from date: " + dateToDownload);
        String releaseFolderPath =
                "Z://TEMP FOR LATER/2019/" + CheckDate.getTodayDate() +
                        "/RECORDPOOL/" + "8th Wonder Pool " + category +
                        getDateForDownload(dateToDownload) + "/";
        new File(releaseFolderPath).mkdirs();
        CustomExecutor downloadMaster = new CustomExecutor(10);
        scrapedLinks.stream()
                .map(link -> "https://pool.8thwonderpromos.com" + link)
                .map(downloadUrl -> new Thread(() ->
                        downloadFile(downloadUrl, releaseFolderPath)))
                .forEach(downloadMaster::submit);
        downloadMaster.WaitUntilTheEnd();
        //  SCHEDULE DOWNLOADED RELEASE
        ewLogger.log("Release Downloaded: " + dateToDownload);
        addToScheduleDB(new File(releaseFolderPath));
        ewLogger.log("Release Scheduled: " + dateToDownload);
    }

    @SneakyThrows
    private static void downloadFile(String url, String releaseFolderPath) {
        @Cleanup CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        get.setHeader("cookie", cookieForAPI);
        get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
        @Cleanup CloseableHttpResponse response = client.execute(get);
        String fileName = response.getFirstHeader("Content-Disposition").getValue()
                .replace("attachment; filename=", "")
                .replaceAll("\"", "");
        File mp3File = new File(releaseFolderPath + fileName);
        ewLogger.log("Downloading file: " + fileName
                + " | " + response.getStatusLine());
        @Cleanup OutputStream outputStream = new FileOutputStream(mp3File);
        response.getEntity().writeTo(outputStream);
        ewLogger.log("Downloaded: " + fileName);
    }

    private static String getDateForDownload(String date) throws java.text.ParseException {
        SimpleDateFormat DATE_FORMAT =
                new SimpleDateFormat("MM.dd.yy", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.setTime(DATE_FORMAT.parse(date));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat("ddMM").format(cal.getTime());
    }

}
