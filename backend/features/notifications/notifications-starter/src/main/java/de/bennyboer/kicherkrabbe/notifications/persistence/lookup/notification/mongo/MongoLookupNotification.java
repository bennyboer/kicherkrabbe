package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupNotification {

    @MongoId
    String id;

    long version;

    MongoOrigin origin;

    MongoTarget target;

    Set<MongoChannel> channels;

    String title;

    String message;

    Instant sentAt;

}
