package de.bennyboer.kicherkrabbe.mailing.settings;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Duration;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class RateLimitSettings {

    Duration duration;

    long limit;

    public static RateLimitSettings of(Duration duration, long limit) {
        notNull(duration, "Duration must be given");
        check(limit >= 0, "Limit must be greater or equal to 0");

        return new RateLimitSettings(duration, limit);
    }

    public static RateLimitSettings init() {
        var aDay = Duration.ofHours(24);

        return new RateLimitSettings(aDay, 100);
    }

    public RateLimitSettings update(Duration duration, long limit) {
        notNull(duration, "Duration must be given");
        check(limit >= 0, "Limit must be greater or equal to 0");

        return withDuration(duration)
                .withLimit(limit);
    }

}
