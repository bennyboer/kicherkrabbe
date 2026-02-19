package de.bennyboer.kicherkrabbe.fabrics.http.api.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdateFabricImagesRequest {

    public long version;

    public String imageId;

    public List<String> exampleImageIds;

}
