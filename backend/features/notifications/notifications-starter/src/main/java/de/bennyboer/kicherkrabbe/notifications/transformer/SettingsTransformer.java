package de.bennyboer.kicherkrabbe.notifications.transformer;

import de.bennyboer.kicherkrabbe.notifications.api.SettingsDTO;
import de.bennyboer.kicherkrabbe.notifications.settings.Settings;

public class SettingsTransformer {

    public static SettingsDTO toApi(Settings settings) {
        var result = new SettingsDTO();
        
        result.version = settings.getVersion().getValue();
        result.systemSettings = SystemSettingsTransformer.toApi(settings.getSystemSettings());

        return result;
    }

}
