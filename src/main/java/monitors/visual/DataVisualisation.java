package monitors.visual;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import mongodb.MongoControl;
import mongodb.mongo.CategoryInfo;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DataVisualisation extends Application {

   public static void main(String[] args) {
      launch(args);
   }

   @Override
   public void start(Stage stage) {
      Scene scene = new Scene(new Group());
      String title = "Releases - Chart by Categories";
      stage.setTitle(title);
      stage.setWidth(550);
      stage.setHeight(450);
      Label caption = new Label("");
      caption.setTextFill(Color.BLACK);
      caption.setBorder(new Border(new BorderStroke(Paint.valueOf(String.valueOf(Color.WHITE)), BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderStroke.THIN)));
      caption.setStyle("-fx-font: 24 arial;");

      MongoControl mongoControl = new MongoControl();
      // get info about releases to compose chart
      MongoCollection<Document> collection = mongoControl.releasesCollection;
      long count = collection.estimatedDocumentCount();
      double onePercent = (double) count / 100;
      HashMap<String, CategoryInfo> categories = new HashMap<>();

      AggregateIterable<Document> aggregate = collection.aggregate(Arrays.asList(
        Aggregates.unwind("$category"),
        Aggregates.group("$category", Accumulators.sum("count", 1))
        )
      );
      for (Document document : aggregate) {
         String category = document.get("_id").toString();
         int sum = (int) document.get("count");
         double percent = (double) sum / onePercent;
         CategoryInfo categoryInfo = new CategoryInfo(category, sum, percent);
         categories.put(category, categoryInfo);
      }
      List<Data> pie = new ArrayList<>();
      for (CategoryInfo categoryInfo : categories.values()) {
         pie.add(new Data(categoryInfo.categoryName, categoryInfo.percent));
      }
      ObservableList<Data> pieChartData = new ObservableListWrapper<>(pie);

      PieChart chart = new PieChart(pieChartData);
      chart.setTitle(title);
      chart.setAnimated(true);
      for (Data data : chart.getData()) {
         data.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
           e -> {
              caption.setTranslateX(e.getSceneX());
              caption.setTranslateY(e.getSceneY());
              caption.setText(categories.get(data.getName()).getCount() + " : " + (int) data.getPieValue() + "%");
           });
      }
      ObservableList<Node> children = ((Group) scene.getRoot()).getChildren();
      children.add(chart);
      children.add(caption);
      stage.setScene(scene);
      stage.show();
   }
}
