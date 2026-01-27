package de.bennyboer.kicherkrabbe.notifications.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class NotificationDTO {

    String id;

    long version;

    OriginDTO origin;

    TargetDTO target;

    Set<ChannelDTO> channels;

    String title;

    String message;

    Instant sentAt;

}
