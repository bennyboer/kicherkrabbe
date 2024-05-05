package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenGenerator;
import de.bennyboer.kicherkrabbe.credentials.http.CredentialsHttpConfig;
import de.bennyboer.kicherkrabbe.credentials.messaging.CredentialsMessaging;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.mongo.MongoCredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
@Import({
        CredentialsAggregateConfig.class,
        CredentialsPermissionsConfig.class,
        CredentialsHttpConfig.class,
        CredentialsMessaging.class,
        SecurityConfig.class
})
public class CredentialsModuleConfig {

    @Bean
    public CredentialsLookupRepo credentialsLookupRepo(ReactiveMongoTemplate template) {
        return new MongoCredentialsLookupRepo(template);
    }

    @Bean
    public CredentialsModule credentialsModule(
            CredentialsService credentialsService,
            CredentialsLookupRepo credentialsLookupRepo,
            @Qualifier("credentialsPermissionsService") PermissionsService permissionsService,
            TokenGenerator tokenGenerator
    ) {
        return new CredentialsModule(credentialsService, credentialsLookupRepo, permissionsService, tokenGenerator);
    }

}
