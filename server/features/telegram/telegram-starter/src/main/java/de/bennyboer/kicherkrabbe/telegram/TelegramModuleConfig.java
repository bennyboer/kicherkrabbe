package de.bennyboer.kicherkrabbe.telegram;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.telegram.external.TelegramApi;
import de.bennyboer.kicherkrabbe.telegram.external.TelegramApiConfig;
import de.bennyboer.kicherkrabbe.telegram.http.TelegramHttpConfig;
import de.bennyboer.kicherkrabbe.telegram.messaging.TelegramMessaging;
import de.bennyboer.kicherkrabbe.telegram.settings.SettingsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;

@Import({
        TelegramAggregateConfig.class,
        TelegramPermissionsConfig.class,
        TelegramHttpConfig.class,
        TelegramMessaging.class,
        TelegramApiConfig.class,
        SecurityConfig.class
})
@Configuration
public class TelegramModuleConfig {

    @Bean
    public TelegramModule telegramModule(
            @Qualifier("telegramSettingsService") SettingsService settingsService,
            @Qualifier("telegramPermissionsService") PermissionsService permissionsService,
            TelegramApi telegramApi,
            ReactiveTransactionManager transactionManager
    ) {
        return new TelegramModule(
                settingsService,
                permissionsService,
                telegramApi,
                transactionManager
        );
    }

}
