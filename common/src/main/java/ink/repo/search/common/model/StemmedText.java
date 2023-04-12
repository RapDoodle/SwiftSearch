package ink.repo.search.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class StemmedText {
    String stemmedText;
    Map<String, Integer> wordFrequencies;
    int stemmedWordCount;
    int maxTf;
}
