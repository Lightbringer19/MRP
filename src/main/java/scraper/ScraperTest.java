package scraper;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.FUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class ScraperTest {
    public static void main(String[] args) throws InterruptedException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        // test();

        System.setProperty("jsse.enableSNIExtension", "false");

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, (chain, authType) -> true);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);

        HttpClientBuilder clientBuilder = HttpClients.custom().setSSLSocketFactory(sslsf);

        String url = "https://mp3poolonline.com/music/download/824494";
        HttpGet get = new HttpGet(url);
        String cookieForAPI = "";
        get.setHeader("cookie", cookieForAPI);

        CloseableHttpResponse httpResponse = clientBuilder.build().execute(get);
        String toString = EntityUtils.toString(httpResponse.getEntity());
        System.out.println(httpResponse.getStatusLine());

        // @Cleanup CloseableHttpClient client = HttpClients.createDefault();
        // // String releaseFolderPath = "Z://";
        // HttpGet get = new HttpGet(url);
        // get.setHeader("cookie", cookieForAPI);
        // @Cleanup CloseableHttpResponse response = client.execute(get);
        // System.out.println(response.getStatusLine());
        // String fileName = "test.mp3";
        // File mp3File = new File(releaseFolderPath + fileName);
        // OutputStream outputStream = new FileOutputStream(mp3File);
        // response.getEntity().writeTo(outputStream);
    }

    public static void test() {
        String html = FUtils.readFile(new File("Z:\\source.html"));
        Document document = Jsoup.parse(html);
        Elements releases = document.select("div[class=innerPlayer1]");
        for (Element release : releases) {
            String date = release.select("p").first().text()
                    .replace("Added On: ", "");
            System.out.println(date);
            Elements tracks = release.select("div>ul>li");
            for (Element track : tracks) {
                Elements links = track.select("div[class=download2 sub-section]");
                for (Element link : links) {
                    String attr = link.select("a").attr("href");
                    if (attr.contains("download/")) {
                        String replace = attr.replace("/sub2", "");
                        System.out.println(replace);
                    }
                }
            }
        }
    }

}
