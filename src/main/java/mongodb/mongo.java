package mongodb;

import com.mongodb.MongoNamespace;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import reactor.core.publisher.Flux;
import utils.Logger;

public class mongo extends Thread {
   
   public static MongoControl MONGO_CONTROL;
   public static Logger LOGGER = new Logger("MONGO UPDATE");
   
   public static void main(String[] args) {
      MONGO_CONTROL = new MongoControl();
      // from MRP to POOLS
      // String sourceDatabaseName = "MRP";
      // String targetDatabaseName = "POOLS";
      // moveCollection(sourceDatabaseName, targetDatabaseName,
      //   "scrapedDMP", "dmp");
      // from FTP to POOLS
      // String[] collectionsToMove = {
      //   // "mp3_pool_downloaded",
      //   // "bpm_downloaded",
      //   // "bpmLatino_downloaded",
      //   // "remixMp4_downloaded",
      //   // "daleMasBajo_downloaded",
      //   // "heavyHits_downloaded",
      //   // "crateConnect_downloaded",
      //   "maletadvj_downloaded"
      // };
      // sourceDatabaseName = "FTP";
      // for (String collection : collectionsToMove) {
      //    moveCollection(sourceDatabaseName, targetDatabaseName, collection,
      //      collection.replace("_downloaded", ""));
      // }
      // moveCollection(sourceDatabaseName, targetDatabaseName,
      //   "scrapedReleases", "scrapedReleases");
      // moveCollection(sourceDatabaseName, targetDatabaseName,
      //   "ew_downloaded", "eightWonder");
      // moveCollection(sourceDatabaseName, targetDatabaseName,
      //   "bj_downloaded", "beatJunkies");
      // moveCollection(sourceDatabaseName, targetDatabaseName,
      //   "hl_downloaded", "headliner");
      // // from FTP to MRP
      // targetDatabaseName = "MRP";
      // moveCollection(sourceDatabaseName, targetDatabaseName,
      //   "schedule", "schedule");
      // FTP renaming
      // moveCollection(sourceDatabaseName, sourceDatabaseName,
      //   "rp_timeStamps", "timeStamps");
      // moveCollection(sourceDatabaseName, sourceDatabaseName,
      //   "rp_downloaded", "ftp_downloaded");
      
   }
   
   public static void renameCollection(String sourceDatabaseName, String collectionName,
                                       String newName) {
      LOGGER.log("Renaming Collection: " +
        sourceDatabaseName +
        " : " + collectionName +
        " : " + newName
      );
      MONGO_CONTROL.mongoClient
        .getDatabase(sourceDatabaseName)
        .getCollection(collectionName).renameCollection(new MongoNamespace(newName));
   }
   
   public static void moveCollection(String sourceDatabaseName, String targetDatabaseName, String collectionName, String targerCollectionName) {
      LOGGER.log("Moving Collection: " +
        sourceDatabaseName +
        " : " + targetDatabaseName +
        " : " + collectionName +
        " : " + targerCollectionName
      );
      FindIterable<Document> documents =
        MONGO_CONTROL.mongoClient
          .getDatabase(sourceDatabaseName)
          .getCollection(collectionName).find();
      MONGO_CONTROL.mongoClient
        .getDatabase(targetDatabaseName)
        .getCollection(targerCollectionName)
        .insertMany(Flux.fromIterable(documents).collectList().block());
      MONGO_CONTROL.mongoClient
        .getDatabase(sourceDatabaseName)
        .getCollection(collectionName).drop();
   }
}


