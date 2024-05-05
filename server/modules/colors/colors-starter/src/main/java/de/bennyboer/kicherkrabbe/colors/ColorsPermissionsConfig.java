package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

@Configuration
public class ColorsPermissionsConfig {

    @Bean("colorsPermissionsRepo")
    public PermissionsRepo colorsPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("colors_permissions", template);
    }

    @Bean("colorsPermissionsService")
    public PermissionsService colorsPermissionsService(
            @Qualifier("colorsPermissionsRepo") PermissionsRepo permissionsRepo
    ) {
        return new PermissionsService(permissionsRepo, event -> Mono.empty());
    }

}
