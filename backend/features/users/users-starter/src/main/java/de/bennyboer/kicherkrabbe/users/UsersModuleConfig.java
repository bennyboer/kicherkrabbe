package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.users.http.UsersHttpConfig;
import de.bennyboer.kicherkrabbe.users.messaging.UsersMessaging;
import de.bennyboer.kicherkrabbe.users.persistence.lookup.UserLookupRepo;
import de.bennyboer.kicherkrabbe.users.persistence.lookup.mongo.MongoUserLookupRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

@Configuration
@Import({
        UsersAggregateConfig.class,
        UsersPermissionsConfig.class,
        UsersMessaging.class,
        UsersHttpConfig.class,
        SecurityConfig.class,
        UsersSecurityConfig.class
})
public class UsersModuleConfig {

    @Bean
    public UserLookupRepo usersLookupRepo(ReactiveMongoTemplate template) {
        return new MongoUserLookupRepo(template);
    }

    @Bean
    public UsersModule usersModule(
            UsersService usersService,
            UserLookupRepo userLookupRepo,
            @Qualifier("usersPermissionsService") PermissionsService permissionsService,
            ReactiveTransactionManager transactionManager
    ) {
        return new UsersModule(usersService, userLookupRepo, permissionsService, transactionManager);
    }

}
