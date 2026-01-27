package de.bennyboer.kicherkrabbe.notifications.transformer;

import de.bennyboer.kicherkrabbe.notifications.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.notification.OriginType;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class OriginTypeTransformer {

    public static OriginTypeDTO toApi(OriginType type) {
        return switch (type) {
            case MAIL -> OriginTypeDTO.MAIL;
        };
    }

    public static OriginType toInternal(OriginTypeDTO type) {
        notNull(type, "Origin type must be given");

        return switch (type) {
            case MAIL -> OriginType.MAIL;
        };
    }

}
