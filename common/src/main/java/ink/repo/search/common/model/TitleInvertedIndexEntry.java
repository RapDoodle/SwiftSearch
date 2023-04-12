package ink.repo.search.common.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "titleInvertedIndex")
public class TitleInvertedIndexEntry extends InvertedIndexEntry {
}
