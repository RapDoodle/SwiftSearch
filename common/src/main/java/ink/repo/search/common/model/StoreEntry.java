package ink.repo.search.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "stores")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class StoreEntry {
    @Id
    String key;
    Double d;
    Integer i;
    String s;
}
