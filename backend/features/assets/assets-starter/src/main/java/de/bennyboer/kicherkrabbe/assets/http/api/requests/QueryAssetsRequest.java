package de.bennyboer.kicherkrabbe.assets.http.api.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryAssetsRequest {

    String searchTerm;

    Set<String> contentTypes;

    String sortProperty;

    String sortDirection;

    long skip;

    long limit;

}
