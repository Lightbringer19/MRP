package mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoControl {
   public final MongoClient mongoClient;
   private final MongoDatabase mrpDB;
   private final MongoDatabase ftpDB;
   public final MongoDatabase poolsDB;
   public final MongoCollection<Document> releasesCollection;
   public final MongoCollection<Document> categoriesCollection;
   public final MongoCollection<Document> tasksCollection;
   public final MongoCollection<Document> scheduleCollection;
   public final MongoCollection<Document> staticImageLinksCollection;
   public final MongoCollection<Document> timeStampsCollection;
   public final MongoCollection<Document> ftpDownloadedCollection;
   public final MongoCollection<Document> djc_skipCollection;
   public final MongoCollection<Document> dmpDownloaded;
   public final MongoCollection<Document> eightWonderDownloaded;
   public final MongoCollection<Document> mp3PoolDownloaded;
   public final MongoCollection<Document> beatJunkiesDownloaded;
   public final MongoCollection<Document> headlinerDownloaded;
   public final MongoCollection<Document> bpmDownloaded;
   public final MongoCollection<Document> bpmLatinoDownloaded;
   public final MongoCollection<Document> scrapedReleases;
   public final MongoCollection<Document> remixMp4Downloaded;
   public final MongoCollection<Document> daleMasBajoDownloaded;
   public final MongoCollection<Document> heavyHitsDownloaded;
   public final MongoCollection<Document> crateConnectDownloaded;
   public final MongoCollection<Document> maletadvjDownloaded;
   public final MongoCollection<Document> avDistrictDownloaded;
   public final MongoCollection<Document> smashVisionDownloaded;
   public final MongoCollection<Document> vjStreetDownloaded;
   public final MongoCollection<Document> proVideo4DJsDownloaded;
   public final MongoCollection<Document> clubDjVideosDownloaded;
   
   public MongoControl() {
      mongoClient = new MongoClient("localhost", 27017);
      
      //mainDB
      mrpDB = mongoClient.getDatabase("MRP");
      releasesCollection = mrpDB.getCollection("releases");
      tasksCollection = mrpDB.getCollection("tasks");
      categoriesCollection = mrpDB.getCollection("categories");
      staticImageLinksCollection = mrpDB.getCollection("staticImageLinks");
      scheduleCollection = mrpDB.getCollection("schedule");
      
      //FTP
      ftpDB = mongoClient.getDatabase("FTP");
      timeStampsCollection = ftpDB.getCollection("timeStamps");
      ftpDownloadedCollection = ftpDB.getCollection("ftp_downloaded");
      djc_skipCollection = ftpDB.getCollection("djc_skip");
      
      //SCRAPING
      poolsDB = mongoClient.getDatabase("POOLS");
   
      scrapedReleases = poolsDB.getCollection("scrapedReleases");
   
      dmpDownloaded = poolsDB.getCollection("dmp");
      eightWonderDownloaded = poolsDB.getCollection("eightWonder");
      beatJunkiesDownloaded = poolsDB.getCollection("beatJunkies");
      headlinerDownloaded = poolsDB.getCollection("headliner");
      mp3PoolDownloaded = poolsDB.getCollection("mp3_pool");
      bpmDownloaded = poolsDB.getCollection("bpm");
      bpmLatinoDownloaded = poolsDB.getCollection("bpmLatino");
      remixMp4Downloaded = poolsDB.getCollection("remixMp4");
      daleMasBajoDownloaded = poolsDB.getCollection("daleMasBajo");
      heavyHitsDownloaded = poolsDB.getCollection("heavyHits");
      crateConnectDownloaded = poolsDB.getCollection("crateConnect");
      maletadvjDownloaded = poolsDB.getCollection("maletadvj");
      avDistrictDownloaded = poolsDB.getCollection("avDistrict");
      smashVisionDownloaded = poolsDB.getCollection("smashVision");
      vjStreetDownloaded = poolsDB.getCollection("vjStreet");
      proVideo4DJsDownloaded = poolsDB.getCollection("proVideo4DJs");
      clubDjVideosDownloaded = poolsDB.getCollection("clubDjVideos");
   }
   
}
