package de.bennyboer.kicherkrabbe.mailbox.transformer;

import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.mailbox.mail.OriginType;

public class OriginTypeTransformer {

    public static OriginTypeDTO toApi(OriginType type) {
        return switch (type) {
            case INQUIRY -> OriginTypeDTO.INQUIRY;
        };
    }

    public static OriginType toInternal(OriginTypeDTO type) {
        return switch (type) {
            case INQUIRY -> OriginType.INQUIRY;
        };
    }

}
