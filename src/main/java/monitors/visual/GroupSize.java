package monitors.visual;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import mongodb.MongoControl;
import org.bson.Document;
import utils.Log;

import java.util.Arrays;

public class GroupSize extends Application {

   public static void main(String[] args) {
      try {
         launch(args);
      } catch (Exception e) {
         Log.write(e, "Data");
      }
   }

   @Override
   public void start(Stage stage) {
      String title = "Size of Groups";
      stage.setTitle(title);
      CategoryAxis xAxis = new CategoryAxis();
      NumberAxis yAxis = new NumberAxis();
      BarChart<String, Number> bc =
        new BarChart<>(xAxis, yAxis);
      bc.setTitle(title);
      xAxis.setLabel("Release Groups");
      yAxis.setLabel("Releases");

      XYChart.Series series1 = new XYChart.Series();
      series1.setName("Releases");
      ObservableList data = series1.getData();

      MongoControl mongoControl = new MongoControl();
      MongoCollection<Document> collection = mongoControl.releasesCollection;
      AggregateIterable<Document> aggregate = collection.aggregate(Arrays.asList(
        Aggregates.unwind("$category"),
        Aggregates.group("$infoAboutRelease.group", Accumulators.sum("count", 1)))
      );
      int other = 0;
      for (Document document : aggregate) {
         if ((int) document.get("count") < 15) {
            other += (int) document.get("count");
         } else if (document.get("_id").toString().contains("RecordPool")) {
         } else {
            data.add(new XYChart.Data(document.get("_id").toString(), document.get("count")));
         }
      }
      data.add(new XYChart.Data("Other", other));
      Scene scene = new Scene(bc, 800, 600);
      bc.getData().addAll(series1);
      stage.setScene(scene);
      stage.show();
   }
}
