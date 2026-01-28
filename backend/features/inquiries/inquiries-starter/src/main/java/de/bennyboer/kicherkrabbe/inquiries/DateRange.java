package de.bennyboer.kicherkrabbe.inquiries;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Comparator;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.time.temporal.ChronoUnit.DAYS;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class DateRange implements Comparable<DateRange> {

    Instant from;

    Instant to;

    public static DateRange of(Instant from, Instant to) {
        notNull(from, "From must be given");
        notNull(to, "To must be given");

        return new DateRange(from, to);
    }

    public static DateRange fullDay(Instant instant) {
        Instant startOfDay = instant.truncatedTo(DAYS);
        Instant endOfDay = startOfDay.plus(1, DAYS);

        return of(startOfDay, endOfDay);
    }

    @Override
    public int compareTo(DateRange other) {
        return Comparator.comparing(DateRange::getFrom)
                .thenComparing(DateRange::getTo)
                .compare(this, other);
    }

}
