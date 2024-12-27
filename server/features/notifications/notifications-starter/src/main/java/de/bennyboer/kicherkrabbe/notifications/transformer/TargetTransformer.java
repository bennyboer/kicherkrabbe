package de.bennyboer.kicherkrabbe.notifications.transformer;

import de.bennyboer.kicherkrabbe.notifications.api.TargetDTO;
import de.bennyboer.kicherkrabbe.notifications.notification.Target;
import de.bennyboer.kicherkrabbe.notifications.notification.TargetId;

public class TargetTransformer {

    public static TargetDTO toApi(Target target) {
        var result = new TargetDTO();

        result.type = TargetTypeTransformer.toApi(target.getType());
        result.id = target.getId().getValue();

        return result;
    }

    public static Target toInternal(TargetDTO target) {
        var type = TargetTypeTransformer.toInternal(target.type);
        var id = TargetId.of(target.id);

        return Target.of(type, id);
    }

}
