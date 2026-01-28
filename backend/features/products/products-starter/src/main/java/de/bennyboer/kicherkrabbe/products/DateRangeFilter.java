package de.bennyboer.kicherkrabbe.products;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class DateRangeFilter {

    @Nullable
    Instant from;

    @Nullable
    Instant to;

    public static DateRangeFilter of(@Nullable Instant from, @Nullable Instant to) {
        check(from == null || to == null || from.isBefore(to), "from must be before to");

        return new DateRangeFilter(from, to);
    }

    public static DateRangeFilter empty() {
        return of(null, null);
    }

    public Optional<Instant> getFrom() {
        return Optional.ofNullable(from);
    }

    public Optional<Instant> getTo() {
        return Optional.ofNullable(to);
    }

    public boolean contains(Instant instant) {
        if (from == null && to == null) {
            return true;
        }

        if (from == null) {
            return instant.isBefore(to);
        }

        if (to == null) {
            return instant.equals(from) || instant.isAfter(from);
        }

        return instant.equals(from) || (instant.isAfter(from) && instant.isBefore(to));
    }

}
