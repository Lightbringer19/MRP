package json.db;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.Document;

@Data
@AllArgsConstructor
public class Task {
    String task;
    String releaseId;

    private String toJson() {
        return new Gson().toJson(this);
    }

    public Document toDoc() {
        return new Document(Document.parse(toJson()));
    }
}
