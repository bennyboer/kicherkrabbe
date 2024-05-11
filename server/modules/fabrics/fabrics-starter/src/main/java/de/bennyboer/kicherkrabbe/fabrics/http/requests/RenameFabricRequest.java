package de.bennyboer.kicherkrabbe.fabrics.http.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class RenameFabricRequest {

    public long version;

    public String name;

}
