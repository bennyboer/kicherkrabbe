package de.bennyboer.kicherkrabbe.mailbox.transformer;

import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.mail.Origin;
import de.bennyboer.kicherkrabbe.mailbox.mail.OriginId;

public class OriginTransformer {

    public static OriginDTO toApi(Origin origin) {
        var result = new OriginDTO();

        result.type = OriginTypeTransformer.toApi(origin.getType());
        result.id = origin.getId().getValue();

        return result;
    }

    public static Origin toInternal(OriginDTO origin) {
        var type = OriginTypeTransformer.toInternal(origin.type);
        var id = OriginId.of(origin.id);

        return Origin.of(type, id);
    }

}
