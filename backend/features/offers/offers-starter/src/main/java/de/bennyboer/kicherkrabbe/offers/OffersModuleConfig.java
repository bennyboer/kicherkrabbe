package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.changes.MessagingResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.offers.http.OffersHttpConfig;
import de.bennyboer.kicherkrabbe.offers.messaging.OffersMessaging;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.OfferLookupRepo;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo.MongoOfferLookupRepo;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.ProductForOfferLookupRepo;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.mongo.MongoProductForOfferLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.Map;

import static de.bennyboer.kicherkrabbe.offers.Actions.READ;

@Configuration
@Import({
        OffersAggregateConfig.class,
        OffersPermissionsConfig.class,
        OffersHttpConfig.class,
        OffersMessaging.class,
        SecurityConfig.class
})
public class OffersModuleConfig {

    @Bean
    public OfferLookupRepo offerLookupRepo(ReactiveMongoTemplate template) {
        return new MongoOfferLookupRepo(template);
    }

    @Bean
    public ProductForOfferLookupRepo productForOfferLookupRepo(ReactiveMongoTemplate template) {
        return new MongoProductForOfferLookupRepo(template);
    }

    @Bean("offerChangesTracker")
    public ResourceChangesTracker offerChangesTracker(
            EventListenerFactory eventListenerFactory,
            PermissionEventListenerFactory permissionEventListenerFactory,
            @Qualifier("offersPermissionsService") PermissionsService permissionsService
    ) {
        return new MessagingResourceChangesTracker(
                eventListenerFactory,
                permissionEventListenerFactory,
                permissionsService,
                ResourceType.of("OFFER"),
                READ,
                event -> {
                    var metadata = event.getMetadata();

                    return Map.of(
                            "aggregateType", metadata.getAggregateType().getValue(),
                            "aggregateId", metadata.getAggregateId().getValue(),
                            "aggregateVersion", metadata.getAggregateVersion().getValue(),
                            "event", event.getEvent()
                    );
                }
        );
    }

    @Bean
    public OffersModule offersModule(
            OfferService offerService,
            @Qualifier("offersPermissionsService") PermissionsService permissionsService,
            OfferLookupRepo offerLookupRepo,
            ProductForOfferLookupRepo productForOfferLookupRepo,
            @Qualifier("offerChangesTracker") ResourceChangesTracker offerChangesTracker
    ) {
        return new OffersModule(
                offerService,
                permissionsService,
                offerLookupRepo,
                productForOfferLookupRepo,
                offerChangesTracker
        );
    }

}
