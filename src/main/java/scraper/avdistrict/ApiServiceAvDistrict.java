package scraper.avdistrict;

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
import org.apache.http.util.EntityUtils;
import org.bson.Document;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public interface ApiServiceAvDistrict {
   
   @SneakyThrows
   default String getDownloadLink(String videoid) {
      String url = "http://www.avdistrict.net/Videos/InitializeDownload";
      HttpPost post = new HttpPost(url);
      post.addHeader("Content-Type", " application/x-www-form-urlencoded; charset=UTF-8");
      post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
      post.setHeader("Cookie", getCookie());
      List<NameValuePair> nameValues = new ArrayList<>();
      nameValues.add(new BasicNameValuePair("videoid", videoid));
      post.setEntity(new UrlEncodedFormEntity(nameValues, HTTP.UTF_8));
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      @Cleanup CloseableHttpResponse response = client.execute(post);
      String data = (String) Document.parse(
        EntityUtils.toString(response.getEntity())).get("data");
      return MessageFormat.format(
        "http://www.avdistrict.net/Handlers/DownloadHandler.ashx?key={0}", data);
   }
   
   String getCookie();
}
