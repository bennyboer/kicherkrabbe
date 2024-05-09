package de.bennyboer.kicherkrabbe.fabrictypes.http.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryFabricTypesResponse {

    long skip;

    long limit;

    long total;

    List<FabricTypeDTO> fabricTypes;

}
