package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.fabrics.http.FabricsHttpConfig;
import de.bennyboer.kicherkrabbe.fabrics.messaging.FabricsMessaging;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.FabricLookupRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.mongo.MongoFabricLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
@Import({
        FabricsAggregateConfig.class,
        FabricsPermissionsConfig.class,
        FabricsHttpConfig.class,
        FabricsMessaging.class,
        SecurityConfig.class
})
public class FabricsModuleConfig {

    @Bean
    public FabricLookupRepo fabricLookupRepo(ReactiveMongoTemplate template) {
        return new MongoFabricLookupRepo(template);
    }

    @Bean
    public FabricsModule fabricsModule(
            FabricService fabricService,
            @Qualifier("fabricsPermissionsService") PermissionsService permissionsService,
            FabricLookupRepo fabricLookupRepo
    ) {
        return new FabricsModule(fabricService, permissionsService, fabricLookupRepo);
    }

}
