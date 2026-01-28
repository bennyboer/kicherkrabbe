package de.bennyboer.kicherkrabbe.mailing.api.requests;

import de.bennyboer.kicherkrabbe.mailing.api.ReceiverDTO;
import de.bennyboer.kicherkrabbe.mailing.api.SenderDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class SendMailRequest {

    SenderDTO sender;

    Set<ReceiverDTO> receivers;

    String subject;

    String text;

}
