package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class OffersPermissionsConfig {

    @Bean("offersPermissionsRepo")
    public PermissionsRepo offersPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("offers_permissions", template);
    }

    @Bean("offersPermissionsService")
    public PermissionsService offersPermissionsService(
            @Qualifier("offersPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
