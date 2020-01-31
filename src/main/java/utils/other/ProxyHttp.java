package utils.other;

import lombok.Cleanup;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class ProxyHttp {
   public static final String AUTH_USER = "";
   public static final String AUTH_PASSWORD = "";
   public static final int PORT = 3199;
   public static final CredentialsProvider CREDS = new BasicCredentialsProvider();
   public static final String HOST = "";
   
   public static String get(String url, String cookie)
     throws IOException {
      String host = "";
      CREDS.setCredentials(new AuthScope(host, PORT), new
        UsernamePasswordCredentials(AUTH_USER, AUTH_PASSWORD));
      HttpClientBuilder builder = HttpClients.custom().setDefaultCredentialsProvider(CREDS);
      
      HttpHost proxyHost = new HttpHost(host, PORT, "http");
      RequestConfig.Builder reqconfigconbuilder = RequestConfig.custom().setProxy(proxyHost);
      RequestConfig config = reqconfigconbuilder.build();
      
      @Cleanup CloseableHttpClient client = builder.build();
      HttpGet get = new HttpGet(url);
      get.setHeader("cookie", cookie);
      get.setConfig(config);
      
      HttpClientContext context = HttpClientContext.create();
      @Cleanup CloseableHttpResponse response = client.execute(get, context);
      // List<URI> redirectURIs = context.getRedirectLocations();
      // if (redirectURIs != null && !redirectURIs.isEmpty()) {
      //     for (URI redirectURI : redirectURIs) {
      //         System.out.println("Redirect URI: " + redirectURI);
      //     }
      //     URI finalURI = redirectURIs.get(redirectURIs.size() - 1);
      //     return finalURI.toString();
      // }
      
      String HTMLResponse = EntityUtils.toString(response.getEntity());
      
      return HTMLResponse;
   }
   
   public static String post(String url, String cookie, String body)
     throws IOException {
      CREDS.setCredentials(new AuthScope(HOST, PORT), new
        UsernamePasswordCredentials(AUTH_USER, AUTH_PASSWORD));
      HttpHost proxyHost = new HttpHost(HOST, PORT, "http");
      RequestConfig config = RequestConfig.custom().setProxy(proxyHost).build();
      
      @Cleanup CloseableHttpClient client = HttpClients.custom()
        .setDefaultCredentialsProvider(CREDS).build();
      
      HttpPost get = new HttpPost(url);
      get.setHeader("cookie", cookie);
      
      get.setConfig(config);
      
      HttpClientContext context = HttpClientContext.create();
      get.addHeader("Content-Type", " application/x-www-form-urlencoded; charset=UTF-8");
      get.setEntity(new StringEntity(body, ContentType.APPLICATION_FORM_URLENCODED));
      @Cleanup CloseableHttpResponse response = client.execute(get, context);
      
      // List<URI> redirectURIs = context.getRedirectLocations();
      // if (redirectURIs != null && !redirectURIs.isEmpty()) {
      //     for (URI redirectURI : redirectURIs) {
      //         System.out.println("Redirect URI: " + redirectURI);
      //     }
      //     URI finalURI = redirectURIs.get(redirectURIs.size() - 1);
      //     return finalURI.toString();
      // }
      
      String HTMLResponse = EntityUtils.toString(response.getEntity());
      
      return HTMLResponse;
   }
   
}
