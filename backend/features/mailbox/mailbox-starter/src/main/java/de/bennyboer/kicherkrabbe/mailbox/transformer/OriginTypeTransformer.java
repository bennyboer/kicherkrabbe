package de.bennyboer.kicherkrabbe.mailbox.transformer;

import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.mailbox.mail.OriginType;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class OriginTypeTransformer {

    public static OriginTypeDTO toApi(OriginType type) {
        return switch (type) {
            case INQUIRY -> OriginTypeDTO.INQUIRY;
        };
    }

    public static OriginType toInternal(OriginTypeDTO type) {
        notNull(type, "OriginTypeDTO must be given");

        return switch (type) {
            case INQUIRY -> OriginType.INQUIRY;
        };
    }

}
