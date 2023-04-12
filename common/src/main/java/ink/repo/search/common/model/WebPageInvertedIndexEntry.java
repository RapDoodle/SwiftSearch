package ink.repo.search.common.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "webPageInvertedIndex")
public class WebPageInvertedIndexEntry extends InvertedIndexEntry {
}
