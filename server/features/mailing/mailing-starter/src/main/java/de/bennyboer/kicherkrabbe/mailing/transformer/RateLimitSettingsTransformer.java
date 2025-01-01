package de.bennyboer.kicherkrabbe.mailing.transformer;

import de.bennyboer.kicherkrabbe.mailing.api.RateLimitSettingsDTO;
import de.bennyboer.kicherkrabbe.mailing.settings.RateLimitSettings;

public class RateLimitSettingsTransformer {

    public static RateLimitSettingsDTO toApi(RateLimitSettings settings) {
        var result = new RateLimitSettingsDTO();

        result.durationInMs = settings.getDuration().toMillis();
        result.limit = settings.getLimit();

        return result;
    }

}
