package de.bennyboer.kicherkrabbe.inquiries.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Duration;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class RateLimitDTO {

    long maxRequests;

    Duration duration;

}
