package de.bennyboer.kicherkrabbe.mailing.transformer;

import de.bennyboer.kicherkrabbe.mailing.api.MailgunSettingsDTO;
import de.bennyboer.kicherkrabbe.mailing.settings.ApiToken;
import de.bennyboer.kicherkrabbe.mailing.settings.MailgunSettings;

public class MailgunSettingsTransformer {

    public static MailgunSettingsDTO toApi(MailgunSettings settings) {
        var result = new MailgunSettingsDTO();

        result.maskedApiToken = settings.getApiToken()
                .map(ApiToken::getMaskedValue)
                .orElse(null);

        return result;
    }

}
