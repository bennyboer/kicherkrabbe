package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.inquiries.http.InquiriesHttpConfig;
import de.bennyboer.kicherkrabbe.inquiries.messaging.InquiriesMessaging;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.InquiryLookupRepo;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.mongo.MongoInquiryLookupRepo;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.RequestRepo;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.mongo.MongoRequestRepo;
import de.bennyboer.kicherkrabbe.inquiries.settings.SettingsService;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.transaction.ReactiveTransactionManager;

import java.time.Clock;
import java.util.Optional;

@Configuration
@Import({
        InquiriesAggregateConfig.class,
        InquiriesPermissionsConfig.class,
        InquiriesHttpConfig.class,
        InquiriesMessaging.class,
        SecurityConfig.class
})
public class InquiriesModuleConfig {

    @Bean
    public InquiryLookupRepo inquiryLookupRepo(ReactiveMongoTemplate template) {
        return new MongoInquiryLookupRepo(template);
    }

    @Bean
    public RequestRepo requestRepo(ReactiveMongoTemplate template) {
        return new MongoRequestRepo(template);
    }

    @Bean
    public InquiriesModule inquiriesModule(
            InquiryService inquiryService,
            @Qualifier("inquiriesSettingsService") SettingsService settingsService,
            InquiryLookupRepo inquiryLookupRepo,
            RequestRepo requestRepo,
            @Qualifier("inquiriesPermissionsService") PermissionsService permissionsService,
            ReactiveTransactionManager transactionManager,
            Optional<Clock> clock
    ) {
        return new InquiriesModule(
                inquiryService,
                settingsService,
                inquiryLookupRepo,
                requestRepo,
                permissionsService,
                transactionManager,
                clock.orElse(Clock.systemUTC())
        );
    }

}
