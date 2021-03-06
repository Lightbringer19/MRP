package json.db;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoAboutRelease {
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
   List<ReleaseNew.Track> trackList;
   
   private String toJson() {
      return new GsonBuilder().serializeNulls().create().toJson(this);
   }
   
   public Document toDoc() {
      return new Document(Document.parse(toJson()));
   }
   
   // @Data
   // @AllArgsConstructor
   // public static class Track {
   //    String title;
   //    String artist;
   //    String trackDuration;
   //
   //    String toLine() {
   //       String line = artist + " - " + title + " (" + trackDuration + ")";
   //       return line;
   //    }
   // }
}
