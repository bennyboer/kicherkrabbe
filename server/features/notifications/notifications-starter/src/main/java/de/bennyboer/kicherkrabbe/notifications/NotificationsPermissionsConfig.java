package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class NotificationsPermissionsConfig {

    @Bean("notificationsPermissionsRepo")
    public PermissionsRepo notificationsPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("notifications_permissions", template);
    }

    @Bean("notificationsPermissionsService")
    public PermissionsService notificationsPermissionsService(
            @Qualifier("notificationsPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
