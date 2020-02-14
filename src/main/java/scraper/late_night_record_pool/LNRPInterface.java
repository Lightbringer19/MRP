package scraper.late_night_record_pool;

import com.mongodb.client.MongoCollection;
import lombok.Cleanup;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import static com.mongodb.client.model.Filters.eq;
import static java.text.MessageFormat.format;

public interface LNRPInterface {
   @NotNull
   default String editType(LNRPApiResponse.LNRPRelease.Version version) {
      String type = version.getType();
      String beforeParenthesis = type;
      if (type.contains("(")) {
         beforeParenthesis = type.substring(0, type.indexOf(" ("));
      }
      type = format("({0}){1}",
        beforeParenthesis, type.replace(beforeParenthesis, ""));
      return type.trim();
   }
   
   default String getResponseFromApi(String apiUrl) {
      try {
         @Cleanup CloseableHttpClient client = HttpClients.createDefault();
         @Cleanup CloseableHttpResponse response = client.execute(new HttpPost(apiUrl));
         return EntityUtils.toString(response.getEntity());
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }
   
   default boolean isNotInDb(String search, String field, MongoCollection<Document> collection) {
      return collection
        .find(eq(field, search)).first() == null;
   }
   
   default boolean isNotInDb(int search, String field, MongoCollection<Document> collection) {
      return collection
        .find(eq(field, search)).first() == null;
   }
}
