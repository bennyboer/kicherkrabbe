package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

@Configuration
public class UsersPermissionsConfig {

    @Bean("usersPermissionsRepo")
    public PermissionsRepo usersPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("users_permissions", template);
    }

    @Bean("usersPermissionsService")
    public PermissionsService usersPermissionsService(
            @Qualifier("usersPermissionsRepo") PermissionsRepo permissionsRepo
    ) {
        return new PermissionsService(permissionsRepo, ignored -> Mono.empty());
    }

}
