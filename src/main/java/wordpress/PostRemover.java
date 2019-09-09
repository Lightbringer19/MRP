package wordpress;

import configuration.YamlConfig;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PostRemover {
   
   private static Scanner IN = new Scanner(System.in);
   private static String mrp_authorization;
   private static Scheduler scheduler = Schedulers
     .newElastic("url-scraper", 10);
   
   @SneakyThrows
   public static void main(String[] args) {
      YamlConfig yamlConfig = new YamlConfig();
      mrp_authorization = yamlConfig.config.getMrp_authorization();
      while (true) {
         System.out.println("Enter post URL(s): ");
         List<String> urls = getUrls();
         System.out.println("==========DELETING==========");
         Flux.fromIterable(urls)
           .parallel(100)
           .runOn(scheduler)
           .flatMap(url -> Mono.just(getId(url)))
           .doOnNext(PostRemover::deletePost)
           .sequential()
           .blockLast();
      }
   }
   
   void delete(String url) {
      System.out.println("Deleting: " + url);
      String id = getId(url);
      deletePost(id);
      System.out.println("Deleted: " + url);
   }
   
   @NotNull
   public static List<String> getUrls() {
      List<String> urls = new ArrayList<>();
      String lineNew;
      while (IN.hasNextLine()) {
         lineNew = IN.nextLine();
         if (lineNew.isEmpty()) {
            break;
         }
         urls.add(lineNew);
      }
      return urls;
   }
   
   @SneakyThrows
   public static void deletePost(String id) {
      while (true) {
         System.out.println("Deleting post (ID): " + id);
         @Cleanup CloseableHttpClient client = HttpClients.createDefault();
         HttpDelete httpDelete = new HttpDelete("https://myrecordpool.com/wp-json/wp/v2/posts/" + id);
         httpDelete.addHeader("Authorization", mrp_authorization);
         @Cleanup CloseableHttpResponse response = client.execute(httpDelete);
         System.out.println(response.getStatusLine());
         if (response.getStatusLine().getStatusCode() == 200) {
            System.out.println("Post Deleted (ID): " + id);
            break;
         } else {
            System.out.println("POST NOT DELETED: " + id);
            Thread.sleep(5000);
         }
      }
   }
   
   @NotNull
   @SneakyThrows
   public static String getId(String url) {
      return Jsoup.connect(url).get()
        .select("article")
        .attr("id")
        .replace("post-", "");
   }
}
