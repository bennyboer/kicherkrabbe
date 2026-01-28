package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.products.counter.CounterService;
import de.bennyboer.kicherkrabbe.products.persistence.CounterEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.products.persistence.ProductEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.products.product.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class ProductsAggregateConfig {

    @Bean("productsProductEventSourcingRepo")
    public EventSourcingRepo productsProductEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("products_product_events", template, new ProductEventPayloadSerializer());
    }

    @Bean("productsProductEventPublisher")
    public MessagingEventPublisher productsProductEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new ProductEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean("productsProductService")
    public ProductService productService(
            @Qualifier("productsProductEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("productsProductEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new ProductService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

    @Bean("productsCounterEventSourcingRepo")
    public EventSourcingRepo productsCounterEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("products_counter_events", template, new CounterEventPayloadSerializer());
    }

    @Bean("productsCounterEventPublisher")
    public MessagingEventPublisher productsCounterEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new CounterEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean("productsCounterService")
    public CounterService counterService(
            @Qualifier("productsCounterEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("productsCounterEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new CounterService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

}
