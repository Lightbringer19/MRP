package monitors.visual;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
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

import static com.mongodb.client.model.Filters.eq;

public class DataSize extends Application {

    @Override
    public void start(Stage stage) {
        String title = "Size of Categories";
        stage.setTitle(title);
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bc =
                new BarChart<>(xAxis, yAxis);
        bc.setTitle(title);
        xAxis.setLabel("Category");
        yAxis.setLabel("GB");

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Size");
        ObservableList data = series1.getData();

        MongoControl mongoControl = new MongoControl();
        MongoCollection<Document> collection = mongoControl.releasesCollection;
        AggregateIterable<Document> aggregate = collection.aggregate(Arrays.asList(
                Aggregates.unwind("$category"),
                Aggregates.group("$category"))
        );
        for (Document document : aggregate) {
            FindIterable<Document> documents = collection.find(eq("category", document.get("_id").toString()));
            long allSize = 0;
            for (Document release : documents) {
                Document infoAboutRelease = (Document) release.get("infoAboutRelease");
                int size = Integer.valueOf(((String) infoAboutRelease.get("size")).replace(" MB", ""));
                allSize += size;
            }
            // System.out.println(allSize / 1024 + "GB " + document.get("_id").toString());
            data.add(new XYChart.Data(document.get("_id").toString(), allSize / 1024));
        }
        Scene scene = new Scene(bc, 800, 600);
        bc.getData().addAll(series1);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            Log.write(e, "Data");
        }
    }
}
