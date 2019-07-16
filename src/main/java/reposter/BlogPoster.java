package reposter;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.BloggerScopes;
import com.google.api.services.blogger.model.Post;
import lombok.SneakyThrows;
import utils.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BlogPoster {
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(BloggerScopes.BLOGGER);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    public static final String TOKENS = "/tokens";
    private static final String APPLICATION_NAME = "Reposter";
    private Blogger blogger;

    void post(String content, String title, List<String> labels, String category) throws InterruptedException {
        Post post = new Post();
        post.setContent(content);
        post.setTitle(title);
        post.setLabels(labels);

        String blogId;
        if (category.contains("RECORDPOOL")) {
            blogId = "1375903955781910474";
        } else if (category.contains("SCENE")) {
            blogId = "1112462949457780777";
        } else {
            blogId = "2147118726551617710";
        }

        // loop until posted
        int sleepMultiplier = 1;
        while (true) {
            try {
                Blogger.Posts.Insert insert = blogger.posts().insert(blogId, post);
                insert.execute();
                break;
            } catch (Exception e) {
                Log.write(e + " Exception Reposter", "Reposter");
                Thread.sleep((1000 * sleepMultiplier) + ThreadLocalRandom.current().nextInt(500, 1000 + 1));
                sleepMultiplier *= 2;
            }
        }
    }

    @SneakyThrows
    BlogPoster() {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        blogger = new Blogger.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME).build();
    }

    private static Credential getCredentials(NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = Test.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//		 Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
