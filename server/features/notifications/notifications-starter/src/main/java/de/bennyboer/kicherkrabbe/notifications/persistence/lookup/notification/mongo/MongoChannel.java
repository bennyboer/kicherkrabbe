package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.mongo;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoChannel {

    MongoChannelType type;

    @Nullable
    String mail;

    @Nullable
    MongoTelegram telegram;

}
