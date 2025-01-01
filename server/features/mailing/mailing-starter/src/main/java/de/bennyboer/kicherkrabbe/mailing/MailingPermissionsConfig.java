package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class MailingPermissionsConfig {

    @Bean("mailingPermissionsRepo")
    public PermissionsRepo mailingPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("mailing_permissions", template);
    }

    @Bean("mailingPermissionsService")
    public PermissionsService mailingPermissionsService(
            @Qualifier("mailingPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
