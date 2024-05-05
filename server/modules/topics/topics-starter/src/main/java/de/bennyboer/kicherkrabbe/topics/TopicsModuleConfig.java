package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.topics.http.TopicsHttpConfig;
import de.bennyboer.kicherkrabbe.topics.messaging.TopicsMessaging;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        TopicsAggregateConfig.class,
        TopicsPermissionsConfig.class,
        TopicsHttpConfig.class,
        TopicsMessaging.class,
        SecurityConfig.class
})
public class TopicsModuleConfig {

    @Bean
    public TopicsModule topicsModule(
            TopicService topicService,
            @Qualifier("topicsPermissionsService") PermissionsService permissionsService
    ) {
        return new TopicsModule(topicService, permissionsService);
    }

}
