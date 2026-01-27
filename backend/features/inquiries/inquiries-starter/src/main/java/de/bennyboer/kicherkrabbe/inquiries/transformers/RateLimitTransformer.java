package de.bennyboer.kicherkrabbe.inquiries.transformers;

import de.bennyboer.kicherkrabbe.inquiries.api.RateLimitDTO;
import de.bennyboer.kicherkrabbe.inquiries.settings.RateLimit;

public class RateLimitTransformer {

    public static RateLimitDTO toApi(RateLimit rateLimit) {
        var result = new RateLimitDTO();

        result.maxRequests = rateLimit.getMaxRequests();
        result.duration = rateLimit.getDuration();

        return result;
    }

    public static RateLimit toInternal(RateLimitDTO rateLimit) {
        return RateLimit.of(rateLimit.maxRequests, rateLimit.duration);
    }

}
