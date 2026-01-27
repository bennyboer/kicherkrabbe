package de.bennyboer.kicherkrabbe.mailbox.api.responses;

import de.bennyboer.kicherkrabbe.mailbox.api.MailDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryMailsResponse {

    long total;

    List<MailDTO> mails;

}
