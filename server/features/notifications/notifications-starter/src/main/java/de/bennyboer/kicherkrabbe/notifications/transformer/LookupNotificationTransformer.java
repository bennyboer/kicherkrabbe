package de.bennyboer.kicherkrabbe.notifications.transformer;

import de.bennyboer.kicherkrabbe.notifications.api.NotificationDTO;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.LookupNotification;

import java.util.List;
import java.util.stream.Collectors;

public class LookupNotificationTransformer {

    public static List<NotificationDTO> toApi(List<LookupNotification> notifications) {
        return notifications.stream()
                .map(LookupNotificationTransformer::toApi)
                .toList();
    }

    public static NotificationDTO toApi(LookupNotification notification) {
        var result = new NotificationDTO();

        result.id = notification.getId().getValue();
        result.version = notification.getVersion().getValue();
        result.origin = OriginTransformer.toApi(notification.getOrigin());
        result.target = TargetTransformer.toApi(notification.getTarget());
        result.channels = notification.getChannels()
                .stream()
                .map(ChannelTransformer::toApi)
                .collect(Collectors.toSet());
        result.title = notification.getTitle().getValue();
        result.message = notification.getMessage().getValue();
        result.sentAt = notification.getSentAt();

        return result;
    }

}
