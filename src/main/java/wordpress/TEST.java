package wordpress;

import json.ResponseInfo;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static wordpress.Poster.MRP_AUTHORIZATION;
import static wordpress.WP_API.postAndGetResponse;

@Log
class TEST {
   @SneakyThrows
   public static void main(String[] args) {
      String url = "https://myrecordpool.com/wp-json/wp/v2/posts/" + 20000;
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      HttpGet get = new HttpGet(url);
      @Cleanup CloseableHttpResponse response = client.execute(get);
      int responseCode = response.getStatusLine().getStatusCode();
      System.out.println(responseCode);
      System.out.println(EntityUtils.toString(response.getEntity()));
   }
   
   @SuppressWarnings("Duplicates")
   public static String createCategory(String category, String parentId) throws ParseException {
      String apiURI = "https://myrecordpool.com/wp-json/wp/v2/categories";
      ResponseInfo responseInfo = postAndGetResponse(new Document()
          .append("name", category).append("parent", parentId).toJson(), apiURI,
        MRP_AUTHORIZATION);
      String categoryID = null;
      if (responseInfo.getCode() == 400) {
         categoryID = ((JSONObject) ((JSONObject) new JSONParser()
           .parse(responseInfo.getJsonBody())).get("data")).get("term_id").toString();
         System.out.println("Category found: " + category + " ID: " + categoryID);
         
      } else if (responseInfo.getCode() == 201) {
         categoryID = new Document(Document.parse(responseInfo.getJsonBody()))
           .get("id").toString();
         System.out.println("Category created: " + category + " ID: " + categoryID);
      }
      return categoryID;
   }
}
