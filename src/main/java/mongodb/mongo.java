package mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import configuration.YamlConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.Document;

import java.util.stream.IntStream;

import static com.mongodb.client.model.Filters.eq;

public class mongo extends Thread {
   
   public static void main(String[] args) {
      // MongoControl mongoControl = new MongoControl();
      YamlConfig yamlConfig = new YamlConfig();
      System.out.println(yamlConfig.config.toString());
      
   }
   
   private static UpdateResult replace(MongoCollection<Document> testCollection, Document document) {
      IntStream.range(0, 5).forEach(i -> document.append("something" + i, (int) (document.get("number")) + 454541));
      return testCollection.replaceOne(eq("_id", document.getObjectId("_id")), document);
   }
   
   @Override
   public void run() {
      main(null);
      
   }
   
   @AllArgsConstructor
   @Data
   public static class CategoryInfo {
      public String categoryName;
      public double percent;
      int count;
   }
}


