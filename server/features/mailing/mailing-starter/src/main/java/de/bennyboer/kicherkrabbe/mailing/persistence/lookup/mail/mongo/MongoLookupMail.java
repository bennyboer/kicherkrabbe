package de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.mongo;

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
public class MongoLookupMail {

    @MongoId
    String id;

    long version;

    MongoSender sender;

    Set<MongoReceiver> receivers;

    String subject;

    String text;

    Instant sentAt;

}
