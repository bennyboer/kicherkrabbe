package de.bennyboer.kicherkrabbe.mailbox.api.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class ReceiveMailResponse {

    String mailId;

    long version;

}
