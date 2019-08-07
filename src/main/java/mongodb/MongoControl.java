package mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoControl {
    
    private final MongoClient mongoClient;
    private final MongoDatabase mrpDB;
    private final MongoDatabase ftpDB;
    public final MongoCollection<Document> releasesCollection;
    public final MongoCollection<Document> tasksCollection;
    public final MongoCollection<Document> scheduleCollection;
    public final MongoCollection<Document> staticImageLinksCollection;
    public final MongoCollection<Document> dayFolderTimeCollection;
    public final MongoCollection<Document> rpDownloadedCollection;
    public final MongoCollection<Document> toDownloadCollection;
    public final MongoCollection<Document> scrapedDMP;
    public final MongoCollection<Document> ewDownloaded;
    public final MongoCollection<Document> mp3PoolDownloaded;
    public final MongoCollection<Document> bjDownloaded;
    public final MongoCollection<Document> hlDownloaded;
    public final MongoCollection<Document> bpmDownloaded;
    public final MongoCollection<Document> bpmLatinoDownloaded;
    
    public MongoControl() {
        mongoClient = new MongoClient("localhost", 27017);
    
        mrpDB = mongoClient.getDatabase("MRP");
        releasesCollection = mrpDB.getCollection("releases");
        tasksCollection = mrpDB.getCollection("tasks");
        staticImageLinksCollection = mrpDB.getCollection("staticImageLinks");
        scrapedDMP = mrpDB.getCollection("scrapedDMP");
    
        ftpDB = mongoClient.getDatabase("FTP");
        scheduleCollection = ftpDB.getCollection("schedule");
        dayFolderTimeCollection = ftpDB.getCollection("rp_timeStamps");
        rpDownloadedCollection = ftpDB.getCollection("rp_downloaded");
        toDownloadCollection = ftpDB.getCollection("rp_toDownload");
    
        ewDownloaded = ftpDB.getCollection("ew_downloaded");
        mp3PoolDownloaded = ftpDB.getCollection("mp3_pool_downloaded");
        bjDownloaded = ftpDB.getCollection("bj_downloaded");
        hlDownloaded = ftpDB.getCollection("hl_downloaded");
        bpmDownloaded = ftpDB.getCollection("bpm_downloaded");
        bpmLatinoDownloaded = ftpDB.getCollection("bpmLatino_downloaded");
    }
    
}
