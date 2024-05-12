package de.bennyboer.kicherkrabbe.fabrictypes.http.api.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class CreateFabricTypeResponse {

    String id;

}
