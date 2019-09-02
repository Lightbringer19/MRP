package json;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PromoPoolsPost {
   String link;
   String HTML;
   private String releaseName;
   private String releaseCategory;
}
