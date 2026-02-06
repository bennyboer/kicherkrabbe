package de.bennyboer.kicherkrabbe.fabrics.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class PublishedFabricDTO {

    String id;

    String alias;

    String name;

    String imageId;

    Set<String> colorIds;

    Set<String> topicIds;

    Set<FabricTypeAvailabilityDTO> availability;

}
