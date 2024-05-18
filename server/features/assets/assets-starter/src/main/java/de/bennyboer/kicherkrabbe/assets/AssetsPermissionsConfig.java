package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class AssetsPermissionsConfig {

    @Bean("assetsPermissionsRepo")
    public PermissionsRepo assetsPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("assets_permissions", template);
    }

    @Bean("assetsPermissionsService")
    public PermissionsService assetsPermissionsService(
            @Qualifier("assetsPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
