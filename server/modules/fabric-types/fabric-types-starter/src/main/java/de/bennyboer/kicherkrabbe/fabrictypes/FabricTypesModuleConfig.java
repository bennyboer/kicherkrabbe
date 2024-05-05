package de.bennyboer.kicherkrabbe.fabrictypes;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.fabrictypes.http.FabricTypesHttpConfig;
import de.bennyboer.kicherkrabbe.fabrictypes.messaging.FabricTypesMessaging;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        FabricTypesAggregateConfig.class,
        FabricTypesPermissionsConfig.class,
        FabricTypesHttpConfig.class,
        FabricTypesMessaging.class,
        SecurityConfig.class
})
public class FabricTypesModuleConfig {

    @Bean
    public FabricTypesModule fabricTypesModule(
            FabricTypeService fabricTypeService,
            @Qualifier("fabricTypesPermissionsService") PermissionsService permissionsService
    ) {
        return new FabricTypesModule(fabricTypeService, permissionsService);
    }

}
