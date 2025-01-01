package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.mailing.external.MailApi;
import de.bennyboer.kicherkrabbe.mailing.external.MailApiConfig;
import de.bennyboer.kicherkrabbe.mailing.http.MailingHttpConfig;
import de.bennyboer.kicherkrabbe.mailing.mail.MailService;
import de.bennyboer.kicherkrabbe.mailing.messaging.MailingMessaging;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.MailLookupRepo;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.mongo.MongoMailLookupRepo;
import de.bennyboer.kicherkrabbe.mailing.settings.SettingsService;
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
        MailingAggregateConfig.class,
        MailingPermissionsConfig.class,
        MailingHttpConfig.class,
        MailingMessaging.class,
        MailingTasks.class,
        MailApiConfig.class,
        SecurityConfig.class
})
@Configuration
public class MailingModuleConfig {

    @Bean("mailingMailLookupRepo")
    public MailLookupRepo mailLookupRepo(ReactiveMongoTemplate template) {
        return new MongoMailLookupRepo("mailing_mails_lookup", template);
    }

    @Bean
    public MailingModule mailingModule(
            @Qualifier("mailingSettingsService") SettingsService settingsService,
            @Qualifier("mailingMailService") MailService mailService,
            @Qualifier("mailingMailLookupRepo") MailLookupRepo mailLookupRepo,
            @Qualifier("mailingPermissionsService") PermissionsService permissionsService,
            MailApi mailApi,
            ReactiveTransactionManager transactionManager,
            Optional<Clock> clock
    ) {
        return new MailingModule(
                settingsService,
                mailService,
                mailLookupRepo,
                permissionsService,
                mailApi,
                transactionManager,
                clock.orElse(Clock.systemUTC())
        );
    }

}
