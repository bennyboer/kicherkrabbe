package de.bennyboer.kicherkrabbe.telegram;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class TelegramPermissionsConfig {

    @Bean("telegramPermissionsRepo")
    public PermissionsRepo telegramPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("telegram_permissions", template);
    }

    @Bean("telegramPermissionsService")
    public PermissionsService telegramPermissionsService(
            @Qualifier("telegramPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
