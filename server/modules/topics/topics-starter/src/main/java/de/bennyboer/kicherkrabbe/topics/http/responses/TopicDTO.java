package de.bennyboer.kicherkrabbe.topics.http.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class TopicDTO {

    String id;

    long version;

    String name;

    Instant createdAt;

}
