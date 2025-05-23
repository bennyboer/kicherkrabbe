package de.bennyboer.kicherkrabbe.mailbox.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class OriginDTO {

    OriginTypeDTO type;

    String id;

}
