package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.colors.http.ColorsHttpConfig;
import de.bennyboer.kicherkrabbe.colors.messaging.ColorsMessaging;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.ColorLookupRepo;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.mongo.MongoColorLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.ResourceType;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
@Import({
        ColorsAggregateConfig.class,
        ColorsPermissionsConfig.class,
        ColorsHttpConfig.class,
        ColorsMessaging.class,
        SecurityConfig.class
})
public class ColorsModuleConfig {

    @Bean
    public ColorLookupRepo colorLookupRepo(ReactiveMongoTemplate template) {
        return new MongoColorLookupRepo(template);
    }

    @Bean
    public AccessibleColorsTracker accessibleColorsTracker(PermissionEventListenerFactory factory) {
        var resourceType = ResourceType.of("COLOR");

        return agent -> factory.listen(
                resourceType,
                "track-accessible-colors-for-agent-%s".formatted(agent.getId().getValue())
        ).map(event -> event.getType().name());
    }

    @Bean
    public ColorsModule colorsModule(
            ColorService colorService,
            @Qualifier("colorsPermissionsService") PermissionsService permissionsService,
            ColorLookupRepo colorLookupRepo,
            AccessibleColorsTracker accessibleColorsTracker
    ) {
        return new ColorsModule(colorService, permissionsService, colorLookupRepo, accessibleColorsTracker);
    }

}
