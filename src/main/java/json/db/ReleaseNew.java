package json.db;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseNew {
   
   String releaseName;
   
   Date date;
   String category;
   
   String pathToLocalFolder;
   String boxComDownloadLink;
   String linkToArt;
   
   String artist;
   String album;
   String genre;
   String released;
   String numberOfTracks;
   String playtime;
   String group;
   String format;
   String bitrate;
   String sampleRate;
   String size;
   List<Track> trackList;
   
   @Data
   @AllArgsConstructor
   public static class Track {
      String title;
      String artist;
      String trackDuration;
   }
   
   private String toJson() {
      return new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create().toJson(this);
   }
   
   public Document toDoc() {
      return new Document(Document.parse(toJson()));
   }
}
