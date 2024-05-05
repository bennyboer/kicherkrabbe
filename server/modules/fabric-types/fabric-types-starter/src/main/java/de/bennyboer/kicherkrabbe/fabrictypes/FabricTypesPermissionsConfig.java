package de.bennyboer.kicherkrabbe.fabrictypes;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

@Configuration
public class FabricTypesPermissionsConfig {

    @Bean("fabricTypesPermissionsRepo")
    public PermissionsRepo fabricTypesPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("fabric_types_permissions", template);
    }

    @Bean("fabricTypesPermissionsService")
    public PermissionsService fabricTypesPermissionsService(
            @Qualifier("fabricTypesPermissionsRepo") PermissionsRepo permissionsRepo
    ) {
        return new PermissionsService(permissionsRepo, event -> Mono.empty());
    }

}
