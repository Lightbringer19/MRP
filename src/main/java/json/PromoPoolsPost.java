package json;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PromoPoolsPost {
    private String releaseName;
    private String releaseCategory;
    String link;
    String HTML;
}
