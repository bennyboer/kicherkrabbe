package de.bennyboer.kicherkrabbe.mailing.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MailDTO {

    String id;

    long version;

    SenderDTO sender;

    Set<ReceiverDTO> receivers;

    String subject;

    String text;

    Instant sentAt;

}
