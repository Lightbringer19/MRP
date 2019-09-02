package json;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class WPPost {
   private String title;
   private String status;
   private String[] categories;
   private String content;
   
   public String toJson() {
      return new Gson().toJson(this);
   }
   
}
