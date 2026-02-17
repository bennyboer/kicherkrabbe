package de.bennyboer.kicherkrabbe.assets.persistence.references.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoAssetReference {

    @MongoId
    String id;

    String assetId;

    String resourceType;

    String resourceId;

}
