package wordpress;

import configuration.YamlConfig;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import utils.Logger;

import java.io.IOException;
import java.util.Scanner;

import static org.bson.Document.parse;
import static wordpress.Poster.MRP_AUTHORIZATION;

public class CategoryDeleter {
   
   private static Logger logger = new Logger("Category Deleter");
   private static final Scanner IN = new Scanner(System.in);
   
   public CategoryDeleter() {
      YamlConfig yamlConfig = new YamlConfig();
      MRP_AUTHORIZATION = yamlConfig.config.getMrp_authorization();
   }
   
   //363767
   @SneakyThrows
   public static void main(String[] args) {
      CategoryDeleter deleter = new CategoryDeleter();
      // deleter.deleteCategory(66769);
      System.out.println("Enter category ID: ");
      int postIdEntered = IN.nextInt();
      for (int categoryID = postIdEntered; categoryID < 300000; categoryID++) {
         try {
            deleter.deleteCategory(categoryID);
         } catch (Exception e) {
            logger.log(e);
            Thread.sleep(5000);
         }
      }
   }
   
   private void deleteCategory(int categoryID) throws IOException {
      logger.log("Preparing       : " + categoryID);
      String url = "https://myrecordpool.com/wp-json/wp/v2/categories/" + categoryID;
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      HttpGet get = new HttpGet(url);
      get.addHeader("Authorization", MRP_AUTHORIZATION);
      @Cleanup CloseableHttpResponse response = client.execute(get);
      int responseCode = response.getStatusLine().getStatusCode();
      if (responseCode == 200) {
         String clean = EntityUtils.toString(response.getEntity());
         String parentID = new Document(parse(clean))
           .get("parent").toString();
         if (parentID.equals("5513")) {
            HttpDelete delete = new HttpDelete(url + "?force=true");
            delete.addHeader("Authorization", MRP_AUTHORIZATION);
            @Cleanup CloseableHttpResponse httpResponse = client.execute(delete);
            // logger.log(httpResponse.getStatusLine().getStatusCode());
            System.out.println(EntityUtils.toString(httpResponse.getEntity()));
         }
      } else {
         logger.log("Error on search : " + categoryID + " |: " + responseCode);
      }
   }
}
