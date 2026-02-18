package de.bennyboer.kicherkrabbe.assets.http.api.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryStorageInfoResponse {

    long usedBytes;

    long limitBytes;

}
