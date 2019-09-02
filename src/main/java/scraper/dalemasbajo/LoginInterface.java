package scraper.dalemasbajo;

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
import org.openqa.selenium.Cookie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

interface LoginInterface {
   
   @SneakyThrows
   default Cookie getCookie(String username, String password) {
      String url = "https://dalemasbajo.com/login/front/";
      HttpPost post = new HttpPost(url);
      post.addHeader("Content-Type", " application/x-www-form-urlencoded; charset=UTF-8");
      post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
      List<NameValuePair> nvps = new ArrayList<>();
      nvps.add(new BasicNameValuePair("email", username));
      nvps.add(new BasicNameValuePair("password", password));
      post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
      @Cleanup CloseableHttpClient client = HttpClients.createDefault();
      @Cleanup CloseableHttpResponse response = client.execute(post);
      Cookie cookie = Arrays.stream(response.getAllHeaders())
        .filter(header -> header.getName().equals("Set-Cookie"))
        .map(header -> header.getElements()[0])
        .map(cookieElem -> new Cookie(cookieElem.getName(), cookieElem.getValue()))
        .reduce((cookie1, cookie2) -> cookie1)
        .get();
      return cookie;
   }
}
