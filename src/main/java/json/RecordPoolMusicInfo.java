package json;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordPoolMusicInfo {
   
   String releaseName;
   String category;
   String mrpLink;
   
   public String toJson() {
      return new Gson().toJson(this);
   }
}
