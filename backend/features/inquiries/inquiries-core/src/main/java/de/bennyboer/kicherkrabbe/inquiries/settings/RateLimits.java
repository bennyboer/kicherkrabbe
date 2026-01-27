package de.bennyboer.kicherkrabbe.inquiries.settings;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Duration;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RateLimits {

    RateLimit perMail;

    RateLimit perIp;

    RateLimit overall;

    public static RateLimits of(RateLimit perMail, RateLimit perIp, RateLimit overall) {
        notNull(perMail, "Per mail rate limit must be given");
        notNull(perIp, "Per IP rate limit must be given");
        notNull(overall, "Overall rate limit must be given");

        return new RateLimits(perMail, perIp, overall);
    }

    public static RateLimits init() {
        var aDay = Duration.ofHours(24);
        
        return new RateLimits(
                RateLimit.of(2, aDay),
                RateLimit.of(2, aDay),
                RateLimit.of(20, aDay)
        );
    }

    @Override
    public String toString() {
        return "RateLimits(perMail=%s, perIp=%s, overall=%s)".formatted(perMail, perIp, overall);
    }

}
