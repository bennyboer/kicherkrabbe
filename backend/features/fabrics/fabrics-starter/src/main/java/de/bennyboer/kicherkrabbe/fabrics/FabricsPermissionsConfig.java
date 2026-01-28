package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class FabricsPermissionsConfig {

    @Bean("fabricsPermissionsRepo")
    public PermissionsRepo fabricsPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("fabrics_permissions", template);
    }

    @Bean("fabricsPermissionsService")
    public PermissionsService fabricsPermissionsService(
            @Qualifier("fabricsPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
