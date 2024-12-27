package de.bennyboer.kicherkrabbe.notifications.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.notifications.NotificationsModule;
import de.bennyboer.kicherkrabbe.notifications.api.OriginDTO;
import de.bennyboer.kicherkrabbe.notifications.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.api.TargetDTO;
import de.bennyboer.kicherkrabbe.notifications.api.TargetTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.api.requests.SendNotificationRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationsMessaging {

    @Bean
    public EventListener onUserCreatedAddPermissionToReadNotificationsAndManageSettingsMsgListener(
            EventListenerFactory factory,
            NotificationsModule module
    ) {
        return factory.createEventListenerForEvent(
                "notifications.user-created-add-permission-to-read-notifications-and-manage-settings",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToReadNotificationsAndManageSettings(userId);
                }
        );
    }

    @Bean
    public EventListener onUserDeletedRemoveNotificationsPermissionsMsgListener(
            EventListenerFactory factory,
            NotificationsModule module
    ) {
        return factory.createEventListenerForEvent(
                "notifications.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean
    public EventListener onNotificationSentUpdateLookupMsgListener(
            EventListenerFactory factory,
            NotificationsModule module
    ) {
        return factory.createEventListenerForEvent(
                "notifications.notification-sent-update-lookup",
                AggregateType.of("NOTIFICATION"),
                EventName.of("SENT"),
                (event) -> {
                    String notificationId = event.getMetadata().getAggregateId().getValue();

                    return module.updateNotificationInLookup(notificationId);
                }
        );
    }

    @Bean
    public EventListener onNotificationDeletedRemoveFromLookupMsgListener(
            EventListenerFactory factory,
            NotificationsModule module
    ) {
        return factory.createEventListenerForEvent(
                "notifications.notification-deleted-remove-from-lookup",
                AggregateType.of("NOTIFICATION"),
                EventName.of("DELETED"),
                (event) -> {
                    String notificationId = event.getMetadata().getAggregateId().getValue();

                    return module.removeNotificationFromLookup(notificationId);
                }
        );
    }

    @Bean
    public EventListener onNotificationSentAllowSystemUserToDeleteNotification(
            EventListenerFactory factory,
            NotificationsModule module
    ) {
        return factory.createEventListenerForEvent(
                "notifications.notification-sent-allow-system-user-to-delete-notification",
                AggregateType.of("NOTIFICATION"),
                EventName.of("SENT"),
                (event) -> {
                    String notificationId = event.getMetadata().getAggregateId().getValue();

                    return module.allowSystemUserToDeleteNotification(notificationId);
                }
        );
    }

    @Bean
    public EventListener onNotificationDeletedRemovePermissionsMsgListener(
            EventListenerFactory factory,
            NotificationsModule module
    ) {
        return factory.createEventListenerForEvent(
                "notifications.notification-deleted-remove-permissions",
                AggregateType.of("NOTIFICATION"),
                EventName.of("DELETED"),
                (event) -> {
                    String notificationId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForNotification(notificationId);
                }
        );
    }

    @Bean
    public EventListener onMailReceivedSendSystemNotificationMsgListener(
            EventListenerFactory factory,
            NotificationsModule module
    ) {
        return factory.createEventListenerForEvent(
                "notifications.mail-received-send-notification",
                AggregateType.of("MAIL"),
                EventName.of("RECEIVED"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    var origin = new OriginDTO();
                    origin.type = OriginTypeDTO.MAIL;
                    origin.id = mailId;

                    var target = new TargetDTO();
                    target.type = TargetTypeDTO.SYSTEM;
                    target.id = "SYSTEM";

                    var request = new SendNotificationRequest();
                    request.origin = origin;
                    request.target = target;
                    request.title = "Neue Nachricht im Postfach";
                    request.message = "Es ist eine neue Nachricht im Postfach eingegangen.";

                    return module.sendNotification(request, Agent.system()).then();
                }
        );
    }

}
