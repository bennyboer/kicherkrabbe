package de.bennyboer.kicherkrabbe.fabrictypes.http.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class FabricTypeDTO {

    String id;

    long version;

    String name;

    Instant createdAt;

}
