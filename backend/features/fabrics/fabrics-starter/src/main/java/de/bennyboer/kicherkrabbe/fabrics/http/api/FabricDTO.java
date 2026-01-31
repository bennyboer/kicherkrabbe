package de.bennyboer.kicherkrabbe.fabrics.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class FabricDTO {

    String id;

    long version;

    String name;

    String imageId;

    Set<String> colorIds;

    Set<String> topicIds;

    Set<FabricTypeAvailabilityDTO> availability;

    boolean published;

    boolean featured;

    Instant createdAt;

}
