package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class ProductsPermissionsConfig {

    @Bean("productsPermissionsRepo")
    public PermissionsRepo productsPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("products_permissions", template);
    }

    @Bean("productsPermissionsService")
    public PermissionsService productsPermissionsService(
            @Qualifier("productsPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
