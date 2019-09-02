package scraper.heavyhits;

import lombok.Cleanup;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import utils.Logger;

public interface DownloadUrlApiService {
   
   default String getDownloadUrl(String url) {
      try {
         @Cleanup CloseableHttpClient client = HttpClients.createDefault();
         HttpGet get = new HttpGet(url);
         RequestConfig.Builder builder = RequestConfig.custom();
         RequestConfig requestConfig = builder.setRedirectsEnabled(false).build();
         get.setConfig(requestConfig);
         get.setHeader("Cookie", getCookie());
         get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
         @Cleanup CloseableHttpResponse response = client.execute(get);
         System.out.println(url + " | " + response.getStatusLine().getStatusCode());
         return response.getFirstHeader("Location").getValue();
      } catch (Exception e) {
         getLogger().log(e);
      }
      return null;
   }
   
   String getCookie();
   
   Logger getLogger();
}
