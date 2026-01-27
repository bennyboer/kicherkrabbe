package de.bennyboer.kicherkrabbe.mailbox.api.requests;

import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class ReceiveMailRequest {

    OriginDTO origin;

    SenderDTO sender;

    String subject;

    String content;

}
