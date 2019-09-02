package scraper.bpm;

import lombok.Cleanup;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface ApiService {
   default List<String> getDownloadInfo(String url) {
      try {
         @Cleanup CloseableHttpClient client = HttpClients.createDefault();
         HttpGet get = new HttpGet(url);
         Builder builder = RequestConfig.custom();
         RequestConfig requestConfig = builder.setRedirectsEnabled(false).build();
         get.setConfig(requestConfig);
         get.setHeader("Cookie", getCookie());
         get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
         @Cleanup CloseableHttpResponse response = client.execute(get);
         System.out.println(url + " " + response.getStatusLine().getStatusCode());
         String cookieForAPI = Arrays.stream(response.getAllHeaders())
           .filter(header -> header.getName().equals("Set-Cookie"))
           .map(header -> header.getElements()[0])
           .map(cookieElem -> cookieElem.getName() + "=" + cookieElem.getValue())
           .collect(Collectors.joining("; "));
         String downloadURL = response.getFirstHeader("Location").getValue();
         List<String> info = new ArrayList<>();
         info.add(downloadURL);
         info.add(cookieForAPI);
         return info;
      } catch (Exception e) {
         getLogger().log(e);
      }
      return null;
   }
   
   String getCookie();
   
   Logger getLogger();
}
