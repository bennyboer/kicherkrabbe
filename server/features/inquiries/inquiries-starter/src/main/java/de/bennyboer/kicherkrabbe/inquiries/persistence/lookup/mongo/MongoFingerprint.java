package de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.mongo;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoFingerprint {

    @Nullable
    String ipAddress;

}
