package scraper.stoped.vjstreet;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.List;

public interface VjStreetApiService {
   
   @SneakyThrows
   default void buyTrack(String code) {
      String url = "https://www.vjstreet.com/en/check_download";
      HttpPost post = new HttpPost(url);
      post.addHeader("Content-Type", " application/x-www-form-urlencoded; charset=UTF-8");
      post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
      post.setHeader("Cookie", getCookie());
      List<NameValuePair> nameValues = new ArrayList<>();
      nameValues.add(new BasicNameValuePair("code", code));
      post.setEntity(new UrlEncodedFormEntity(nameValues, HTTP.UTF_8));
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      @Cleanup CloseableHttpResponse response = client.execute(post);
      System.out.println(code + " " + response.getStatusLine());
   }
   
   String getCookie();
}
