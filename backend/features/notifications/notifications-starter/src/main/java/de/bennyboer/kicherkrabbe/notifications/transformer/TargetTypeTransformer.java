package de.bennyboer.kicherkrabbe.notifications.transformer;

import de.bennyboer.kicherkrabbe.notifications.api.TargetTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.notification.TargetType;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class TargetTypeTransformer {

    public static TargetTypeDTO toApi(TargetType type) {
        return switch (type) {
            case SYSTEM -> TargetTypeDTO.SYSTEM;
        };
    }

    public static TargetType toInternal(TargetTypeDTO type) {
        notNull(type, "Target type must be given");

        return switch (type) {
            case SYSTEM -> TargetType.SYSTEM;
        };
    }

}
