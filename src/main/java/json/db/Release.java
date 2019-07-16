package json.db;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Release {
    String releaseName;
    String category;
    String pathToLocalFolder;
    String boxComDownloadLink;
    String mrpPostLink;
    InfoAboutRelease infoAboutRelease;
    Scraped scraped;

    private String toJson() {
        return new GsonBuilder().serializeNulls().create().toJson(this);
    }

    public Document toDoc() {
        return new Document(Document.parse(toJson()));
    }

    public String getTrackList() {
        return getInfoAboutRelease().getTrackList().stream().map(InfoAboutRelease.Track::toLine).collect(Collectors.joining("<br>"));
    }

    public String getRedditTrackList() {
        return getInfoAboutRelease().getTrackList().stream().map(InfoAboutRelease.Track::toLine).collect(Collectors.joining("\n\n"));
    }
}
