package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class CredentialsPermissionsConfig {

    @Bean("credentialsPermissionsRepo")
    public PermissionsRepo credentialsPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("credentials_permissions", template);
    }

    @Bean("credentialsPermissionsService")
    public PermissionsService credentialsPermissionsService(
            @Qualifier("credentialsPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
