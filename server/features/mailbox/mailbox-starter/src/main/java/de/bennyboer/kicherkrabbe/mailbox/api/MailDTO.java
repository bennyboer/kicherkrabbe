package de.bennyboer.kicherkrabbe.mailbox.api;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MailDTO {

    String id;

    long version;

    OriginDTO origin;

    SenderDTO sender;

    String subject;

    String content;

    Instant receivedAt;

    @Nullable
    Instant readAt;

}
