package de.bennyboer.kicherkrabbe.mailbox.api.responses;

import de.bennyboer.kicherkrabbe.mailbox.api.MailDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryMailResponse {

    MailDTO mail;

}
