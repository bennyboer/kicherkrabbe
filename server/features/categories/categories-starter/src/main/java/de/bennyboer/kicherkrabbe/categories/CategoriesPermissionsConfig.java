package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class CategoriesPermissionsConfig {

    @Bean("categoriesPermissionsRepo")
    public PermissionsRepo categoriesPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("categories_permissions", template);
    }

    @Bean("categoriesPermissionsService")
    public PermissionsService categoriesPermissionsService(
            @Qualifier("categoriesPermissionsRepo") PermissionsRepo permissionsRepo,
            PermissionsEventPublisher eventPublisher
    ) {
        return new PermissionsService(permissionsRepo, eventPublisher);
    }

}
