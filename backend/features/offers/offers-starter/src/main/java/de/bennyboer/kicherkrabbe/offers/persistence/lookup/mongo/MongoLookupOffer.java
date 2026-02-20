package de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupOffer {

    @MongoId
    String id;

    long version;

    MongoProduct product;

    List<String> imageIds;

    Set<MongoLink> links;

    Set<MongoFabricCompositionItem> fabricCompositionItems;

    MongoPricing pricing;

    MongoNotes notes;

    boolean published;

    boolean reserved;

    Instant createdAt;

    Instant archivedAt;

}
