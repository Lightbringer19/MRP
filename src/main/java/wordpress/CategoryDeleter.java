package wordpress;

import configuration.YamlConfig;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static wordpress.Poster.MRP_AUTHORIZATION;

@Slf4j
public class CategoryDeleter {
   
   private static Logger logger = new Logger("Category Deleter");
   // private static final Scanner IN = new Scanner(System.in);
   
   private final CloseableHttpClient client;
   private static List<Integer> importantIDs;
   private Scheduler scheduler;
   
   public CategoryDeleter() {
      YamlConfig yamlConfig = new YamlConfig();
      MRP_AUTHORIZATION = yamlConfig.config.getMrp_authorization();
      client = HttpClients.createDefault();
      scheduler = Schedulers.newParallel("Scheduler", 16);
      importantIDs = new ArrayList<>();
      importantIDs.add(115); //beat
      importantIDs.add(5514); //genre
      importantIDs.add(1051); //scene
      importantIDs.add(1052); //recordpool
   }
   
   //363767
   @SneakyThrows
   public static void main(String[] args) {
      CategoryDeleter deleter = new CategoryDeleter();
      try {
         for (int i = 1; i < 10_000; i++) {
            try {
               deleter.checkCategoryOnPage(i);
            } catch (Exception e) {
               logger.log(e);
               Thread.sleep(5000);
            }
         }
      } finally {
         deleter.client.close();
         deleter.scheduler.dispose();
      }
      // deleter.test();
      // System.out.println("Enter category ID: ");
      // int postIdEntered = IN.nextInt();
      // for (int categoryID = postIdEntered; categoryID < 300000; categoryID++) {
      //    try {
      //       deleter.deleteCategory(categoryID, client);
      //    } catch (Exception e) {
      //       logger.log(e);
      //       Thread.sleep(5000);
      //    }
      // }
   }
   
   @SneakyThrows
   public void checkCategoryOnPage(Integer i) {
      String template = "https://myrecordpool.com/wp-json/wp/v2/categories?parent=0" +
        "&per_page=100&page={0}";
      logger.log("Page number     : " + i + " Start");
      HttpGet getCategories = new HttpGet(MessageFormat.format(template, i));
      getCategories.addHeader("Authorization", MRP_AUTHORIZATION);
      @Cleanup CloseableHttpResponse response = client.execute(getCategories);
      String clean = EntityUtils.toString(response.getEntity());
      JSONArray IDs = (JSONArray) new JSONParser().parse(clean);
      //noinspection unchecked
      Flux.fromIterable(IDs)
        .map(o -> Integer.valueOf(((JSONObject) o).get("id").toString()))
        .filter(o -> !importantIDs.contains(o))
        .parallel()
        .runOn(scheduler)
        .doOnNext(o -> deleteCategory(Integer.parseInt(o.toString())))
        .sequential()
        .blockLast();
      logger.log("Page number     : " + i + " " + response.getStatusLine().toString());
   }
   
   @SneakyThrows
   private void deleteCategory(int categoryID) {
      logger.log("Preparing       : " + categoryID);
      String url = "https://myrecordpool.com/wp-json/wp/v2/categories/" + categoryID;
      // HttpGet get = new HttpGet(url);
      // get.addHeader("Authorization", MRP_AUTHORIZATION);
      // @Cleanup CloseableHttpResponse response = client.execute(get);
      // int responseCode = response.getStatusLine().getStatusCode();
      // if (responseCode == 200) {
      HttpDelete delete = new HttpDelete(url + "?force=true");
      delete.addHeader("Authorization", MRP_AUTHORIZATION);
      @Cleanup CloseableHttpResponse httpResponse = client.execute(delete);
      // if (httpResponse.getStatusLine().getStatusCode())
      logger.log("Category        : " + categoryID + " " +
        EntityUtils.toString(httpResponse.getEntity()));
      // } else {
      //    logger.log("Error on search : " + categoryID + " |: " + responseCode);
      // }
   }
}
