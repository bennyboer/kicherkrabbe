package de.bennyboer.kicherkrabbe.products.persistence.lookup.product.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupProduct {

    @MongoId
    String id;

    long version;

    String number;

    List<String> images;

    List<MongoLink> links;

    MongoFabricComposition fabricComposition;

    MongoNotes notes;

    Instant producedAt;

    Instant createdAt;

}
