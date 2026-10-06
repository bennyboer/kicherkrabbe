package de.bennyboer.kicherkrabbe.embroideries.http.api.requests;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class CreateEmbroideryRequest {

    String name;

    String image;

    Set<String> categories;

}
