package de.bennyboer.kicherkrabbe.embroideries.http.api;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class PublishedEmbroideryDTO {

    String id;

    String name;

    String alias;

    String image;

    Set<String> categories;

}
