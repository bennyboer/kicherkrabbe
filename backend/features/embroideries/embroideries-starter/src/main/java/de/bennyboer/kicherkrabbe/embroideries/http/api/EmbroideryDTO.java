package de.bennyboer.kicherkrabbe.embroideries.http.api;

import java.time.Instant;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class EmbroideryDTO {

    String id;

    long version;

    boolean published;

    boolean featured;

    String name;

    String image;

    Set<String> categories;

    Instant createdAt;

}
