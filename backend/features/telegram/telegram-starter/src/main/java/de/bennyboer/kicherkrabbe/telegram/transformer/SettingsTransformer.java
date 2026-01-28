package de.bennyboer.kicherkrabbe.telegram.transformer;

import de.bennyboer.kicherkrabbe.telegram.api.SettingsDTO;
import de.bennyboer.kicherkrabbe.telegram.settings.Settings;

public class SettingsTransformer {

    public static SettingsDTO toApi(Settings settings) {
        var result = new SettingsDTO();

        result.version = settings.getVersion().getValue();
        result.botSettings = BotSettingsTransformer.toApi(settings.getBotSettings());

        return result;
    }

}
