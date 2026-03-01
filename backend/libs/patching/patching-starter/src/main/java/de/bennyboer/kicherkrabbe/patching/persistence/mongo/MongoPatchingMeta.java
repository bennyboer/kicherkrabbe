package de.bennyboer.kicherkrabbe.patching.persistence.mongo;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoPatchingMeta {

    @MongoId
    String id;

    int version;

    @Nullable
    String lockedBy;

    @Nullable
    Instant lockedAt;

}
