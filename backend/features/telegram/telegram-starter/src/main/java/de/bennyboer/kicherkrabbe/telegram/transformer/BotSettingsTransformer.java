package de.bennyboer.kicherkrabbe.telegram.transformer;

import de.bennyboer.kicherkrabbe.telegram.api.BotSettingsDTO;
import de.bennyboer.kicherkrabbe.telegram.settings.ApiToken;
import de.bennyboer.kicherkrabbe.telegram.settings.BotSettings;

public class BotSettingsTransformer {

    public static BotSettingsDTO toApi(BotSettings botSettings) {
        var result = new BotSettingsDTO();

        result.maskedApiToken = botSettings.getApiToken()
                .map(ApiToken::getMaskedValue)
                .orElse(null);

        return result;
    }

}
