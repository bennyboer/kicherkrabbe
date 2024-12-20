package de.bennyboer.kicherkrabbe.inquiries.api.responses;

import de.bennyboer.kicherkrabbe.inquiries.api.RateLimitsDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QuerySettingsResponse {

    boolean enabled;

    RateLimitsDTO rateLimits;

}
