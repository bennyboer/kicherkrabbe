package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoTelegram {

    String chatId;

}
