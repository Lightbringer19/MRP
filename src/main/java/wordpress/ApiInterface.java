package wordpress;

import json.ResponseInfo;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public interface ApiInterface {
   @SneakyThrows
   static String getClean(String url, String cookie) {
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      HttpGet get = new HttpGet(url);
      get.setHeader("cookie", cookie);
      @Cleanup CloseableHttpResponse response = client.execute(get);
      return EntityUtils.toString(response.getEntity());
   }
   
   static String postClean(String url, String cookie, String body) throws IOException {
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      
      HttpPost get = new HttpPost(url);
      get.setHeader("cookie", cookie);
      
      get.addHeader("Content-Type", " application/x-www-form-urlencoded; charset=UTF-8");
      get.setEntity(new StringEntity(body, ContentType.APPLICATION_FORM_URLENCODED));
      @Cleanup CloseableHttpResponse response = client.execute(get);
      
      return EntityUtils.toString(response.getEntity());
   }
   
   @SneakyThrows
   default ResponseInfo postAndGetResponse(String JSON_BODY, String apiURI,
                                           String authorizationHeader) {
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      HttpPost httpPost = new HttpPost(apiURI);
      httpPost.addHeader("Authorization", authorizationHeader);
      httpPost.addHeader("Content-Type", "application/json");
      httpPost.setEntity(new StringEntity(JSON_BODY, ContentType.APPLICATION_JSON));
      @Cleanup CloseableHttpResponse postResponse = client.execute(httpPost);
      String jsonResponse = EntityUtils.toString(postResponse.getEntity());
      return new ResponseInfo(postResponse.getStatusLine().getStatusCode(), jsonResponse);
   }
}
