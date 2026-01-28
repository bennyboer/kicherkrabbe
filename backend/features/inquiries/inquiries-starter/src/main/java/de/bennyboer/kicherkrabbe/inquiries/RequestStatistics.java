package de.bennyboer.kicherkrabbe.inquiries;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class RequestStatistics {

    DateRange dateRange;

    long totalRequests;

    public static RequestStatistics of(DateRange dateRange, long totalRequests) {
        notNull(dateRange, "Date range must be given");
        check(totalRequests >= 0, "Total requests must not be negative");

        return new RequestStatistics(dateRange, totalRequests);
    }

    public static RequestStatistics empty(DateRange dateRange) {
        return of(dateRange, 0);
    }

    public RequestStatistics add(RequestStatistics other) {
        check(dateRange.equals(other.dateRange), "Date ranges must be equal");

        return withTotalRequests(totalRequests + other.totalRequests);
    }

}
