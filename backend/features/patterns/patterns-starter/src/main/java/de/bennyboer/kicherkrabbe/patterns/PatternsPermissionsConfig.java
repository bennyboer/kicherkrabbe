package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class PatternsPermissionsConfig {

    @Bean("patternsPermissionsRepo")
    public PermissionsRepo patternsPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("patterns_permissions", template);
    }

    @Bean("patternsPermissionsService")
    public PermissionsService patternsPermissionsService(
            @Qualifier("patternsPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
