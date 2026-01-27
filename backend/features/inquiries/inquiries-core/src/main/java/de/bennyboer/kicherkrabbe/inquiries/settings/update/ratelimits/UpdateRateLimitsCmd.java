package de.bennyboer.kicherkrabbe.inquiries.settings.update.ratelimits;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.inquiries.settings.RateLimits;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateRateLimitsCmd implements Command {

    RateLimits rateLimits;

    public static UpdateRateLimitsCmd of(RateLimits rateLimits) {
        notNull(rateLimits, "Rate limits must be given");

        return new UpdateRateLimitsCmd(rateLimits);
    }

}
