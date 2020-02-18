package scraper.masspool;

import lombok.SneakyThrows;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import scraper.abstraction.Scraper;
import utils.FUtils;

import java.io.File;
import java.util.List;

import static utils.CheckDate.getCurrentYear;
import static utils.CheckDate.getTodayDate;
import static utils.Constants.tagsDir;

public class MassPoolScraper extends Scraper {
   
   private boolean scrapingAlbums;
   
   public MassPoolScraper() {
      USERNAME = yamlConfig.getMasspool_username();
      PASS = yamlConfig.getMasspool_password();
   
      loginUrl = "https://www.masspoolmp3.com/Secure/MainLogin.aspx";
      nameFieldNavigator = By.id("Content_txtUsername");
      passFieldNavigator = By.id("Content_txtPassword");
      submitButtonNavigator = By.id("Content_cmdSubmit");
   
      dateFormat = "MM-dd-yyyy";
      releaseName = "Mass Pool";
   
      scrapingAlbums = false;
   }
   
   public static void main(String[] args) {
      MassPoolScraper massPoolScraper = new MassPoolScraper();
      massPoolScraper.start();
   }
   
   @Override
   @SneakyThrows
   public void afterDriverCreation() {
      driver.get("http://www.masspoolmp3.com/members/downloads/Dance");
      Actions action = new Actions(driver);
      WebElement choiceMenu = driver.findElement(
        By.cssSelector("#dl_table_length > label:nth-child(1) > select:nth-child(1)"));
      choiceMenu.click();
      for (int i = 0; i < 5; i++) {
         Thread.sleep(500);
         action.sendKeys(choiceMenu, Keys.DOWN).build().perform();
      }
   }
   
   @Override
   // @SneakyThrows
   public void scrapingStage() {
      downloadCategory("Dance",
        "http://www.masspoolmp3.com/members/downloads/Dance");
      downloadCategory("Hip-Hop",
        "http://www.masspoolmp3.com/members/downloads/HipHop");
      downloadCategory("Underground",
        "http://www.masspoolmp3.com/members/downloads/Underground");
      downloadCategory("Trance-Tech",
        "http://www.masspoolmp3.com/members/downloads/Trance");
      downloadCategory("Electro-House",
        "http://www.masspoolmp3.com/members/downloads/Electro-House");
      // TODO: 02.01.2020 ACTIVATE LATIN LATER WHEN THERE WILL BE ENOUGH DATES: AT LEAST 5
      // downloadCategory("Latin-International",
      // "http://www.masspoolmp3.com/members/downloads/Latin");
      downloadAlbums();
   }
   
   private void downloadAlbums() {
      scrapingAlbums = true;
      downloadCategory("Albums", "https://www.masspoolmp3.com/members/Albums.aspx");
      scrapingAlbums = false;
   }
   
   private void downloadCategory(String category, String url) {
      driver.get(url);
      downloaded = mongoControl.poolsDB.getCollection(
        "masspool_" + category.replaceAll("-", ""));
      releaseName = "Mass Pool " + category;
      logger.log("Scraping: " + category);
      mainOperation();
      logger.log("Scraped: " + category);
   }
   
   @Override
   public String scrapeFirstDate(String html) {
      return Jsoup.parse(html)
        .select("table[id=dl_table]>tbody>tr").first()
        .select("td").get(2)
        .text();
   }
   
   @Override
   public String previousDateOnThisPage(String html, String firstDate) {
      return Jsoup.parse(html)
        .select("table[id=dl_table]>tbody>tr")
        .stream()
        .map(element -> element.select("td").get(2).text())
        .filter(date -> !date.equals(firstDate))
        .findFirst()
        .orElse(null);
   }
   
   @Override
   public void scrapeAllLinksOnPage(String html, String downloadDate, String firstDate, List<String> scrapedLinks) {
      Elements trackInfos = Jsoup.parse(html).select("table[id=dl_table]>tbody>tr");
      for (Element trackInfo : trackInfos) {
         String trackDate = trackInfo.select("td").get(2).text();
         if (trackDate.equals(downloadDate)) {
            String trackName = trackInfo.select("td").get(1).text();
            String downloadPartLink = trackInfo.select("td").get(1)
              .select("a").attr("href");
            String downloadUrl = "https://www.masspoolmp3.com" +
              downloadPartLink.replaceAll(" ", "%20");
            String downloadLink = getDownloadLink(downloadUrl);
            System.out.println(trackName + " | " + downloadLink);
            if (downloadLink != null) {
               scrapedLinks.add(downloadLink);
            }
         }
      }
   }
   
   @Override
   protected void scrapeAndDownloadRelease(String firstDate, String downloadDate, String releaseName) {
      if (scrapingAlbums) {
         List<String> scrapedLinks = scrapeLinks(firstDate, downloadDate);
         downloadAlbumLinks(scrapedLinks);
      } else {
         super.scrapeAndDownloadRelease(firstDate, downloadDate, releaseName);
      }
   }
   
   private void downloadAlbumLinks(List<String> scrapedLinks) {
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
      unzipAll(releaseFolderPath);
      scheduleAll(releaseFolderPath);
      getLogger().log("All Album Releases Downloaded and Scheduled");
   }
   
   @SneakyThrows
   private void scheduleAll(String releaseFolderPath) {
      File folderWithAlbumReleases = new File(releaseFolderPath);
      String recodrpoolDirPath =
        folderWithAlbumReleases.getParentFile().getAbsolutePath() + File.separator;
      for (File releaseFolder : folderWithAlbumReleases.listFiles()) {
         //rename
         String renamedReleasePath =
           recodrpoolDirPath + "Mass Pool - " + releaseFolder.getName();
         FileUtils.moveDirectory(releaseFolder, new File(renamedReleasePath));
         //schedule
         FUtils.writeFile(tagsDir.replace("\\Scrapers", ""),
           releaseFolder.getName() + ".json", renamedReleasePath);
         getLogger().log("Release added to tag queue: " + releaseFolder.getName());
      }
   }
   
   @SneakyThrows
   private void unzipAll(String releaseFolderPath) {
      File folderWithZipReleases = new File(releaseFolderPath);
      for (File zipRelease : folderWithZipReleases.listFiles()) {
         ZipFile zipFile = new ZipFile(zipRelease.getAbsolutePath());
         zipFile.extractAll(releaseFolderPath);
         zipRelease.delete();
      }
   }
   
   public String getDownloadLink(String downloadUrl) {
      if (scrapingAlbums) {
         return downloadUrl;
      } else {
         return getLocation(getLocation(downloadUrl));
      }
   }
   
   @Override
   public void nextPage() {
      driver.findElement(By.id("dl_table_next")).click();
   }
}
