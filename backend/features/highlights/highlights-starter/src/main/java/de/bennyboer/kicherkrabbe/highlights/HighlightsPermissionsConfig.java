package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class HighlightsPermissionsConfig {

    @Bean("highlightsPermissionsRepo")
    public PermissionsRepo highlightsPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("highlights_permissions", template);
    }

    @Bean("highlightsPermissionsService")
    public PermissionsService highlightsPermissionsService(
            @Qualifier("highlightsPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
