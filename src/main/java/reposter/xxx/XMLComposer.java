package reposter.xxx;

import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import json.db.Release;
import mongodb.MongoControl;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class XMLComposer {
   
   private static int id;
   private static int version;
   
   public static void main(String[] args) throws IOException, ParseException {
      MongoControl mongoControl = new MongoControl();
      // Document taskDoc = mongoControl.tasksCollection.find(eq("task", "repost")).first();
      FindIterable<Document> documents = mongoControl.tasksCollection.find(eq("task", "repost"));
      String startTime = "01:00:00";
      Calendar cal = Calendar.getInstance();
      cal.setTime(new SimpleDateFormat("HH:mm:ss").parse(startTime));
      id = 9;
      version = 0;
      for (Document taskDoc : documents) {
         if (taskDoc != null) {
            ObjectId releaseId = new ObjectId(taskDoc.get("releaseId").toString());
            // use extracted ID to find DOC in main DB and cast to Object
            Document foundDoc = mongoControl.releasesCollection.find(eq("_id", releaseId)).first();
            foundDoc.remove("_id");
            Release release = new Gson().fromJson(foundDoc.toJson(), Release.class);
            String title = release.getReleaseName();
            // String content = release.getTrackList() + "<p>Download here:<br><a href=\"" + release.getMrpPostLink() + "\">" + release.getMrpPostLink() + "</a></p>";
            // String artist = release.getInfoAboutRelease().getArtist();
            // String releaseGroup = release.getInfoAboutRelease().getGroup();
            String genre = release.getInfoAboutRelease().getGenre().replaceAll("[^a-zA-Z]", "");
            List<String> categories = Arrays.asList(release.getCategory(), genre);
            //blog
            // blog(cal, title, content, categories);
            //WP
            // WP(cal, title, content, categories);
         }
      }
   }
   
   public static void WP(Calendar cal, String title, String content, List<String> categories) throws IOException {
      StringBuilder wp = new StringBuilder();
      wp.append("<item>");
      wp.append("<title>").append(title).append("</title>");
      String sanitizedTitle = getString(title);
      String link = "http://scenedownload.com/2019/06/03/" + sanitizedTitle + "/";
      wp.append("<link>").append(link).append("</link>");
      cal.add(Calendar.SECOND, 1);
      String time = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
      wp.append("<pubDate>Mon, 03 Jun 2019 " + time + " +0000</pubDate>\n" +
        "\t\t<dc:creator><![CDATA[hotbox]]></dc:creator>");
      wp.append("<guid isPermaLink=\"false\">").append(link).append("</guid>\n").append("\t\t<description></description>");
      wp.append("<content:encoded><![CDATA[").append(content).append("]]></content:encoded>");
      wp.append("<excerpt:encoded><![CDATA[]]></excerpt:encoded>");
      wp.append("<wp:post_id>").append(id).append("</wp:post_id>");
      id++;
      wp.append("<wp:post_date><![CDATA[2019-06-03 " + time + "]]></wp:post_date>\n" +
        "\t\t<wp:post_date_gmt><![CDATA[2019-06-03 " + time + "]]></wp:post_date_gmt>\n" +
        "\t\t<wp:comment_status><![CDATA[open]]></wp:comment_status>\n" +
        "\t\t<wp:ping_status><![CDATA[open]]></wp:ping_status>\n" +
        "\t\t<wp:post_name><![CDATA[" + sanitizedTitle + "]]></wp:post_name>\n" +
        "\t\t<wp:status><![CDATA[publish]]></wp:status>\n" +
        "\t\t<wp:post_parent>0</wp:post_parent>\n" +
        "\t\t<wp:menu_order>0</wp:menu_order>\n" +
        "\t\t<wp:post_type><![CDATA[post]]></wp:post_type>\n" +
        "\t\t<wp:post_password><![CDATA[]]></wp:post_password>\n" +
        "\t\t<wp:is_sticky>0</wp:is_sticky>");
      
      for (String cat : categories) {
         String catNice = getString(cat);
         wp.append("<category domain=\"category\" nicename=\"").append(catNice).append("\"><![CDATA[").append(cat).append("]]></category>");
      }
      wp.append("</item>");
      System.out.println(wp.toString());
      String fileName = "wp" + version + ".xml";
      File file = new File(fileName);
      long size = file.length();
      double kb = (double) size / 1024;
      BufferedWriter writer = new BufferedWriter(
        new FileWriter(fileName, true));
      writer.newLine();
      writer.write(wp.toString());
      writer.close();
      if (kb > 1950) {
         version++;
      }
   }
   
   public static String getString(String title) {
      String s = title.toLowerCase().replaceAll(" ", "-").replaceAll("[^a-zA-Z0-9 .-]", "");
      while (true) {
         if (s.contains("--")) {
            s = s.replace("--", "-");
         } else {
            break;
         }
      }
      return s;
   }
   
   public static void blog(Calendar cal, String title, String content, List<String> categories) throws IOException {
      cal.add(Calendar.SECOND, 1);
      String time = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
      String head = "<entry>\n" +
        "    <id>tag:blogger.com,1999:blog-" + 1 + ".post-" + 1 + "</id>\n" +
        "    <published>2019-06-05T" + time + ".001-07:00</published>\n" +
        "    <updated>2019-06-05T" + time + ".082-07:00</updated>\n" +
        "    <category scheme='http://schemas.google.com/g/2005#kind' term='http://schemas.google.com/blogger/2008/kind#post'/>";
      
      String category = " <category scheme='http://www.blogger.com/atom/ns#' term='REPLACE'/>";
      
      StringBuilder blog = new StringBuilder();
      blog.append(head);
      for (String cat : categories) {
         blog.append(category.replace("REPLACE", sanitize(cat)));
      }
      blog.append(" <title type='text'>").append(title).append("</title>");
      String replaceAll = sanitize(content);
      blog.append(" <content type='html'>").append(replaceAll).append("</content>");
      blog.append("</entry>");
      System.out.println(blog.toString());
      BufferedWriter writer = new BufferedWriter(
        new FileWriter("blog" + ".xml", true));
      writer.newLine();
      writer.write(blog.toString());
      writer.close();
   }
   
   public static String sanitize(String content) {
      return content.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&#39;");
   }
}
