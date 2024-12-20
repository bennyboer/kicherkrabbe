package de.bennyboer.kicherkrabbe.inquiries.api.responses;

import de.bennyboer.kicherkrabbe.inquiries.api.RequestStatisticsDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryRequestStatisticsResponse {

    List<RequestStatisticsDTO> statistics;

}
