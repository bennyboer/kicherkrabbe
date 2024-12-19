package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class InquiriesPermissionsConfig {

    @Bean("inquiriesPermissionsRepo")
    public PermissionsRepo inquiriesPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("inquiries_permissions", template);
    }

    @Bean("inquiriesPermissionsService")
    public PermissionsService inquiriesPermissionsService(
            @Qualifier("inquiriesPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
