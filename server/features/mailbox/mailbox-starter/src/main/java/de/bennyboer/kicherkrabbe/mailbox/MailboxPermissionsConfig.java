package de.bennyboer.kicherkrabbe.mailbox;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class MailboxPermissionsConfig {

    @Bean("mailboxPermissionsRepo")
    public PermissionsRepo mailboxPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("mailbox_permissions", template);
    }

    @Bean("mailboxPermissionsService")
    public PermissionsService mailboxPermissionsService(
            @Qualifier("mailboxPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
