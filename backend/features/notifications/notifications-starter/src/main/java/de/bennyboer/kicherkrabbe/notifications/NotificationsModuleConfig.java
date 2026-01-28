package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.notifications.http.NotificationsHttpConfig;
import de.bennyboer.kicherkrabbe.notifications.messaging.NotificationsMessaging;
import de.bennyboer.kicherkrabbe.notifications.notification.NotificationService;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.NotificationLookupRepo;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.mongo.MongoNotificationLookupRepo;
import de.bennyboer.kicherkrabbe.notifications.settings.SettingsService;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.ReactiveTransactionManager;

import java.time.Clock;
import java.util.Optional;

@EnableScheduling
@Import({
        NotificationsAggregateConfig.class,
        NotificationsPermissionsConfig.class,
        NotificationsHttpConfig.class,
        NotificationsMessaging.class,
        NotificationsTasks.class,
        SecurityConfig.class
})
@Configuration
public class NotificationsModuleConfig {

    @Bean
    public NotificationLookupRepo notificationLookupRepo(ReactiveMongoTemplate template) {
        return new MongoNotificationLookupRepo(template);
    }

    @Bean
    public NotificationsModule notificationsModule(
            NotificationService notificationService,
            @Qualifier("notificationsSettingsService") SettingsService settingsService,
            NotificationLookupRepo notificationLookupRepo,
            @Qualifier("notificationsPermissionsService") PermissionsService permissionsService,
            ReactiveTransactionManager transactionManager,
            Optional<Clock> clock
    ) {
        return new NotificationsModule(
                notificationService,
                settingsService,
                notificationLookupRepo,
                permissionsService,
                transactionManager,
                clock.orElse(Clock.systemUTC())
        );
    }

}
