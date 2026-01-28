package de.bennyboer.kicherkrabbe.inquiries.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class RequestStatisticsDTO {

    DateRangeDTO dateRange;

    long totalRequests;

}
