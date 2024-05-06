package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class TopicsPermissionsConfig {

    @Bean("topicsPermissionsRepo")
    public PermissionsRepo topicsPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("topics_permissions", template);
    }

    @Bean("topicsPermissionsService")
    public PermissionsService topicsPermissionsService(
            @Qualifier("topicsPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
