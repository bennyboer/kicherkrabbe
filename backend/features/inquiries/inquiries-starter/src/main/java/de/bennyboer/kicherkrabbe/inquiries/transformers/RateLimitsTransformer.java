package de.bennyboer.kicherkrabbe.inquiries.transformers;

import de.bennyboer.kicherkrabbe.inquiries.api.RateLimitsDTO;
import de.bennyboer.kicherkrabbe.inquiries.settings.RateLimits;

public class RateLimitsTransformer {

    public static RateLimitsDTO toApi(RateLimits rateLimits) {
        var result = new RateLimitsDTO();

        result.perMail = RateLimitTransformer.toApi(rateLimits.getPerMail());
        result.perIp = RateLimitTransformer.toApi(rateLimits.getPerIp());
        result.overall = RateLimitTransformer.toApi(rateLimits.getOverall());

        return result;
    }

    public static RateLimits toInternal(RateLimitsDTO rateLimits) {
        return RateLimits.of(
                RateLimitTransformer.toInternal(rateLimits.perMail),
                RateLimitTransformer.toInternal(rateLimits.perIp),
                RateLimitTransformer.toInternal(rateLimits.overall)
        );
    }

}
