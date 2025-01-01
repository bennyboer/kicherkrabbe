package de.bennyboer.kicherkrabbe.mailing.api.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class SendMailResponse {

    String id;

    long version;

}
