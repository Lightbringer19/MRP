package poster;

import com.google.gson.Gson;
import json.db.InfoAboutRelease;
import json.db.Release;
import json.db.ReleaseNew;
import json.db.Task;
import lombok.SneakyThrows;
import org.bson.Document;
import org.bson.types.ObjectId;
import utils.Log;

import java.util.Date;

import static com.mongodb.client.model.Filters.eq;
import static poster.Poster.MONGO_CONTROL;

public interface PosterInterface {
   
   default void post(String releaseId) {
      Document releaseDoc = MONGO_CONTROL.releasesCollection
        .find(eq("_id", new ObjectId(releaseId))).first();
      Release release = new Gson().fromJson(releaseDoc.toJson(), Release.class);
      // CONVERT RELEASE AND INSERT TO NEW DB
      ReleaseNew releaseNew = convertRelease(release);
      Document document = releaseNew.toDoc();
      document.append("date", new Date());
      MONGO_CONTROL.releasesNewCollection.insertOne(document);
      
      Log.write("Posted: " + release.getReleaseName() + "| In: " + release.getCategory(),
        "Poster");
      
      // add REPOST TASK to que
      Task task = new Task("repost", document.getObjectId("_id").toString());
      MONGO_CONTROL.tasksCollection.insertOne(task.toDoc());
   }
   
   @SneakyThrows
   default ReleaseNew convertRelease(Release releaseOld) {
      InfoAboutRelease infoAboutRelease = releaseOld.getInfoAboutRelease();
      ReleaseNew release = new ReleaseNew();
      release.setReleaseName(releaseOld.getReleaseName());
      release.setCategory(releaseOld.getCategory());
      release.setPathToLocalFolder(releaseOld.getPathToLocalFolder());
      release.setBoxComDownloadLink(releaseOld.getBoxComDownloadLink());
      release.setLinkToArt(infoAboutRelease.getLinkToArt());
      release.setArtist(infoAboutRelease.getArtist());
      release.setAlbum(infoAboutRelease.getAlbum());
      release.setGenre(infoAboutRelease.getGenre());
      release.setReleased(infoAboutRelease.getReleased());
      release.setNumberOfTracks(infoAboutRelease.getNumberOfTracks());
      release.setPlaytime(infoAboutRelease.getPlaytime());
      release.setGroup(infoAboutRelease.getGroup());
      release.setFormat(infoAboutRelease.getFormat());
      release.setBitrate(infoAboutRelease.getBitrate());
      release.setSampleRate(infoAboutRelease.getSampleRate());
      release.setSize(infoAboutRelease.getSize());
      release.setTrackList(infoAboutRelease.getTrackList());
      return release;
   }
}
