package de.bennyboer.kicherkrabbe.notifications.transformer;

import de.bennyboer.kicherkrabbe.notifications.api.SystemSettingsDTO;
import de.bennyboer.kicherkrabbe.notifications.settings.SystemSettings;

public class SystemSettingsTransformer {

    public static SystemSettingsDTO toApi(SystemSettings systemSettings) {
        var result = new SystemSettingsDTO();

        result.enabled = systemSettings.isEnabled();
        result.channels = ActivatableChannelTransformer.toApi(systemSettings.getChannels());

        return result;
    }

}
