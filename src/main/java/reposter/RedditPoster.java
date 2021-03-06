package reposter;

import configuration.YamlConfig;
import lombok.SneakyThrows;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.SubmissionKind;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.references.SubredditReference;
import utils.Log;

import static java.lang.Thread.sleep;
import static java.util.concurrent.ThreadLocalRandom.current;

public class RedditPoster {
   
   private static String username;
   private static String password;
   private static String clientId;
   private static String clientSecret;
   private final SubredditReference scenedownload;
   private final SubredditReference myRecordPool;
   private final SubredditReference beatportmusic;
   
   public RedditPoster() {
      YamlConfig yamlConfig = new YamlConfig();
      username = yamlConfig.config.getReddit_username();
      password = yamlConfig.config.getReddit_password();
      clientId = yamlConfig.config.getReddit_client_id();
      clientSecret = yamlConfig.config.getReddit_client_secret();
      
      Credentials oauthCreds = Credentials.script(username, password, clientId, clientSecret);
      UserAgent userAgent = new UserAgent("windows", "reposter.mrp", "2.0.0", username);
      RedditClient reddit = OAuthHelper.automatic(new OkHttpNetworkAdapter(userAgent), oauthCreds);
      scenedownload = reddit.subreddit("scenedownload");
      myRecordPool = reddit.subreddit("MyRecordPool");
      beatportmusic = reddit.subreddit("beatportmusic");
   }
   
   public static void main(String[] args) {
      RedditPoster redditPoster = new RedditPoster();
      redditPoster.post("RECORDPOOL", "TEST", "TEST");
   }
   
   @SneakyThrows
   public void post(String category, String title, String content) {
      int sleepMultiplier = 1;
      while (true) {
         try {
            if (category.contains("SCENE")) {
               scenedownload.submit(SubmissionKind.SELF, title, content, true);
            } else if (category.contains("RECORDPOOL")) {
               myRecordPool.submit(SubmissionKind.SELF, title, content, true);
            } else {
               beatportmusic.submit(SubmissionKind.SELF, title, content, true);
            }
            break;
         } catch (Exception e) {
            boolean too_long = e.toString().contains("TOO_LONG");
            Log.write(e + " Exception Reposter", "Reposter");
            if (too_long) {
               break;
            }
            sleep((1000 * sleepMultiplier) + current().nextInt(500, 1000 + 1));
            sleepMultiplier *= 2;
         }
      }
      
   }
}
