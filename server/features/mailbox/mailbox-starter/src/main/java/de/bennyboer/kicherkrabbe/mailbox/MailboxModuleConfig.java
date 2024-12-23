package de.bennyboer.kicherkrabbe.mailbox;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.mailbox.http.MailboxHttpConfig;
import de.bennyboer.kicherkrabbe.mailbox.mail.MailService;
import de.bennyboer.kicherkrabbe.mailbox.messaging.MailboxMessaging;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.MailLookupRepo;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.mongo.MongoMailLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration
@Import({
        MailboxAggregateConfig.class,
        MailboxPermissionsConfig.class,
        MailboxHttpConfig.class,
        MailboxMessaging.class,
        SecurityConfig.class
})
public class MailboxModuleConfig {

    @Bean
    public MailLookupRepo mailLookupRepo(ReactiveMongoTemplate template) {
        return new MongoMailLookupRepo(template);
    }

    @Bean
    public MailboxModule mailboxModule(
            MailService mailService,
            MailLookupRepo mailLookupRepo,
            @Qualifier("mailboxPermissionsService") PermissionsService permissionsService,
            ReactiveTransactionManager transactionManager
    ) {
        return new MailboxModule(mailService, mailLookupRepo, permissionsService, transactionManager);
    }

}
