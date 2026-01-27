package de.bennyboer.kicherkrabbe.mailing.settings.ratelimit.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Duration;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateRateLimitCmd implements Command {

    Duration duration;

    long limit;

    public static UpdateRateLimitCmd of(Duration duration, long limit) {
        notNull(duration, "Duration must be given");
        check(duration.isPositive(), "Duration must be positive");
        check(!duration.isZero(), "Duration must not be zero");
        check(limit >= 0, "Limit must be greater or equal to 0");

        return new UpdateRateLimitCmd(duration, limit);
    }

}
