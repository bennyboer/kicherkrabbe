package de.bennyboer.kicherkrabbe.inquiries.settings;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Duration;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RateLimit {

    long maxRequests;

    Duration duration;

    public static RateLimit of(long maxRequests, Duration duration) {
        check(maxRequests > 0, "Max requests must be greater than 0");
        notNull(duration, "Duration must be given");
        check(duration.isPositive(), "Duration must be positive");
        check(duration.compareTo(Duration.ofDays(30)) <= 0, "Duration must not be greater than 30 days");

        return new RateLimit(maxRequests, duration);
    }

    @Override
    public String toString() {
        return "RateLimit(maxRequests=%d, duration=%s)".formatted(maxRequests, duration);
    }

}
