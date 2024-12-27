package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.notifications.http.NotificationsHttpConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
        NotificationsHttpConfig.class
})
@Configuration
public class NotificationsModuleConfig {

    @Bean
    public NotificationsModule notificationsModule() {
        return new NotificationsModule();
    }

}
