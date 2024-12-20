package de.bennyboer.kicherkrabbe.inquiries.transformers;

import de.bennyboer.kicherkrabbe.inquiries.RequestStatistics;
import de.bennyboer.kicherkrabbe.inquiries.api.RequestStatisticsDTO;

import java.util.List;

public class RequestStatisticsTransformer {

    public static List<RequestStatisticsDTO> toApi(List<RequestStatistics> statistics) {
        return statistics.stream()
                .map(RequestStatisticsTransformer::toApi)
                .toList();
    }

    public static RequestStatisticsDTO toApi(RequestStatistics statistics) {
        var result = new RequestStatisticsDTO();

        result.dateRange = DateRangeTransformer.toApi(statistics.getDateRange());
        result.totalRequests = statistics.getTotalRequests();

        return result;
    }

}
