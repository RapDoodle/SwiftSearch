package ink.repo.search.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(value = "indexedWebpages")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class IndexedWebPage {
    public static final int BODY = 0;
    public static final int TITLE = 1;

    @Id
    private String id;
    @Indexed
    private String url;
    private String title;
    private Date createdDate;
    private Date lastFetchedDate;
    private Date lastModifiedDate;
    private String html;
    private String plainText;
    private String stemmedBody;
    private String stemmedTitle;
    private Map<String, Integer> bodyWordFrequencies;
    private Map<String, Integer> titleWordFrequencies;
    private List<String> referencedBy;
    private List<String> referencesTo;
    private Double pageRank;
    private Integer bodyStemmedWordCount;
    private Integer titleStemmedWordCount;

    public Map<String, Integer> getWordFrequencies(int type) {
        return switch (type) {
            case BODY -> this.bodyWordFrequencies;
            case TITLE -> this.titleWordFrequencies;
            default -> null;
        };
    }

    public String getText(int type) {
        return switch (type) {
            case BODY -> this.plainText;
            case TITLE -> this.title;
            default -> null;
        };
    }

    public String getStemmedText(int type) {
        return switch (type) {
            case BODY -> this.stemmedBody;
            case TITLE -> this.stemmedTitle;
            default -> null;
        };
    }

    public Integer getStemmedWordCount(int type) {
        return switch (type) {
            case BODY -> this.bodyStemmedWordCount;
            case TITLE -> this.titleStemmedWordCount;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return "IndexedWebPage{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", createdDate=" + createdDate +
                ", lastFetchedDate=" + lastFetchedDate +
                ", lastModifiedDate=" + lastModifiedDate +
                ", pageRank=" + pageRank +
                ", bodyStemmedWordCount=" + bodyStemmedWordCount +
                ", titleStemmedWordCount=" + titleStemmedWordCount +
                '}';
    }
}
