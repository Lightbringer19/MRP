package collector;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static wordpress.Poster.MRP_AUTHORIZATION;

class ImageUploader {
   
   static String uploadImage(File imageFile) {
      try {
         while (true) {
            CloseableHttpClient client = HttpClients.createDefault();
            String uri = "https://myrecordpool.com/wp-json/wp/v2/media";
            HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader("Authorization",
              MRP_AUTHORIZATION);
            httpPost.addHeader("Content-Type", "image");
            httpPost.addHeader("content-disposition",
              "attachment; filename=" + imageFile.getName());
            InputStream imageToUpload = new FileInputStream(imageFile.getAbsolutePath());
            byte[] image = IOUtils.toByteArray(imageToUpload);
            imageToUpload.close();
            httpPost.setEntity(new ByteArrayEntity(image));
            CloseableHttpResponse response = client.execute(httpPost);
            String jsonResponse = EntityUtils.toString(response.getEntity());
            client.close();
            if (response.getStatusLine().getStatusCode() != 201) {
               System.out.println(response.getStatusLine());
               Log.write("Not uploaded: " + imageFile.getName() + " " +
                 response.getStatusLine(), "Collector");
               System.out.println(jsonResponse);
               Thread.sleep(5000);
            } else {
               JSONObject post = (JSONObject) new JSONParser().parse(jsonResponse);
               response.close();
               return ((JSONObject) post.get("guid")).get("raw").toString();
            }
         }
      } catch (Exception e) {
         Log.write("Exception uploadIMAGE: " + e, "Collector");
         Log.write(e, "API_IMAGE_Errors_Trace");
      }
      return null;
   }
   
}
