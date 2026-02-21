package de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.mongo;

import de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo.MongoFabricCompositionItem;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo.MongoLink;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoProductForOfferLookup {

    @MongoId
    String id;

    long version;

    String number;

    List<String> images;

    Set<MongoLink> links;

    Set<MongoFabricCompositionItem> fabricCompositionItems;

}
