package wordpress;

import json.ResponseInfo;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static wordpress.Poster.MRP_AUTHORIZATION;
import static wordpress.WP_API.postAndGetResponse;

@Log
class TEST {
    @SneakyThrows
    public static void main(String[] args) {
        // YamlConfig yamlConfig = new YamlConfig();
        // MRP_AUTHORIZATION = yamlConfig.config.getMrp_authorization();
        // String[] categories = {
        // };
        // Map<String, String> categoriesAndIDs = new HashMap<>();
        // for (String category : categories) {
        //     String parentId = "29";
        //     String categoryID = createCategory(category, parentId);
        //     categoriesAndIDs.put(category, categoryID);
        // }
        // MONGO_CONTROL.categoriesCollection.insertOne(
        //    new Document("name", "RECORDPOOL VIDEOS")
        //       .append("categoriesAndIDs", categoriesAndIDs));
        
    }
    
    @SuppressWarnings("Duplicates")
    public static String createCategory(String category, String parentId) throws ParseException {
        String apiURI = "https://myrecordpool.com/wp-json/wp/v2/categories";
        ResponseInfo responseInfo = postAndGetResponse(new Document()
              .append("name", category).append("parent", parentId).toJson(), apiURI,
           MRP_AUTHORIZATION);
        String categoryID = null;
        if (responseInfo.getCode() == 400) {
            categoryID = ((JSONObject) ((JSONObject) new JSONParser()
               .parse(responseInfo.getJsonBody())).get("data")).get("term_id").toString();
            System.out.println("Category found: " + category + " ID: " + categoryID);
            
        } else if (responseInfo.getCode() == 201) {
            categoryID = new Document(Document.parse(responseInfo.getJsonBody()))
               .get("id").toString();
            System.out.println("Category created: " + category + " ID: " + categoryID);
        }
        return categoryID;
    }
}
