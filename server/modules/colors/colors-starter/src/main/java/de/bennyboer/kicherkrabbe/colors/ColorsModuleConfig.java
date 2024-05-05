package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.colors.http.ColorsHttpConfig;
import de.bennyboer.kicherkrabbe.colors.messaging.ColorsMessaging;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
    public ColorsModule colorsModule(
            ColorService colorService,
            @Qualifier("colorsPermissionsService") PermissionsService permissionsService
    ) {
        return new ColorsModule(colorService, permissionsService);
    }

}
