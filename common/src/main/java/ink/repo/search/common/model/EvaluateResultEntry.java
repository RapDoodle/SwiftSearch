package ink.repo.search.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EvaluateResultEntry {
    private String url;
    private String title;
    private List<String> referencedBy;
    private List<String> referencesTo;
    private Date lastModifiedDate;
    private Double score;
    private LinkedHashMap<String, Integer> matchedWords;
    private Integer contentLength;

    public String getFormattedScore() {
        return String.format("%.5f", this.score);
    }

    public String getFormattedContentLength() {
        if (this.contentLength == null)
            return "";
        long bytes = this.contentLength;
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), unit);
    }

    public String getFormattedMatchedWords() {
        if (this.matchedWords == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : this.matchedWords.entrySet()) {
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue());
            sb.append(", ");
        }
        if (this.matchedWords.size() > 0) {
            sb.delete(sb.length() - 2, sb.length() - 1);
        }
        return sb.toString();
    }
}
