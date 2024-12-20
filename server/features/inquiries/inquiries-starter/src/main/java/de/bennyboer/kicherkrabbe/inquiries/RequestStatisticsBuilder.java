package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.Request;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RequestStatisticsBuilder {

    Map<DateRange, RequestStatistics> statisticsPerDateRange;

    public static RequestStatisticsBuilder init() {
        return new RequestStatisticsBuilder(new HashMap<>());
    }

    public RequestStatisticsBuilder count(Request request) {
        DateRange dateRange = DateRange.fullDay(request.getCreatedAt());

        RequestStatistics statistics = statisticsPerDateRange.getOrDefault(
                dateRange,
                RequestStatistics.empty(dateRange)
        );
        statistics = statistics.add(RequestStatistics.of(dateRange, 1));

        statisticsPerDateRange.put(dateRange, statistics);

        return this;
    }

    public List<RequestStatistics> build() {
        return statisticsPerDateRange.values()
                .stream()
                .sorted(Comparator.comparing(RequestStatistics::getDateRange))
                .toList();
    }

}
