package de.bennyboer.kicherkrabbe.users.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.users.UsersModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsersMessaging {

    @Bean
    public EventListener onUserCreatedUpdateLookupMsgListener(
            EventListenerFactory factory,
            UsersModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-created-update-lookup",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (metadata, version, payload) -> {
                    String userId = metadata.getAggregateId().getValue();

                    System.out.println("Updating lookup for new user " + userId);
                    return module.updateUserInLookup(userId);
                }
        );
    }

    @Bean
    public EventListener onUserCreatedAddPermissionsMsgListener(
            EventListenerFactory factory,
            UsersModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-created-add-permissions",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (metadata, version, payload) -> {
                    String userId = metadata.getAggregateId().getValue();

                    System.out.println("Adding permissions for new user " + userId);
                    return module.addPermissionsForNewUser(userId);
                }
        );
    }

    @Bean
    public EventListener onUserDeletedUpdateLookupMsgListener(
            EventListenerFactory factory,
            UsersModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-deleted-update-lookup",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (metadata, version, payload) -> {
                    String userId = metadata.getAggregateId().getValue();

                    return module.removeUserFromLookup(userId);
                }
        );
    }

    @Bean
    public EventListener onUserDeletedRemovePermissionsMsgListener(
            EventListenerFactory factory,
            UsersModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (metadata, version, payload) -> {
                    String userId = metadata.getAggregateId().getValue();

                    return module.removePermissionsOnUser(userId);
                }
        );
    }

}
