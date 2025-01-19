package de.bennyboer.kicherkrabbe.products.api.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdateProducedAtDateRequest {

    long version;

    Instant producedAt;

}
