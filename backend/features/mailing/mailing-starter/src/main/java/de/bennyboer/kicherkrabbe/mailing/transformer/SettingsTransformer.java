package de.bennyboer.kicherkrabbe.mailing.transformer;

import de.bennyboer.kicherkrabbe.mailing.api.SettingsDTO;
import de.bennyboer.kicherkrabbe.mailing.settings.Settings;

public class SettingsTransformer {

    public static SettingsDTO toApi(Settings settings) {
        var result = new SettingsDTO();

        result.version = settings.getVersion().getValue();
        result.rateLimit = RateLimitSettingsTransformer.toApi(settings.getRateLimit());
        result.mailgun = MailgunSettingsTransformer.toApi(settings.getMailgun());

        return result;
    }

}
