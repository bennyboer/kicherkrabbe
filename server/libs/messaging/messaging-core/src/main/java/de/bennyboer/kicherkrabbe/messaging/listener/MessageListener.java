package de.bennyboer.kicherkrabbe.messaging.listener;

import com.rabbitmq.client.Delivery;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.AcknowledgableDelivery;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class MessageListener {

    private final ReactiveTransactionManager transactionManager;

    private final Supplier<Flux<AcknowledgableDelivery>> deliveries;

    private final String name;

    private final Function<Delivery, Mono<Void>> handler;

    public MessageListener(
            ReactiveTransactionManager transactionManager,
            Supplier<Flux<AcknowledgableDelivery>> deliveries,
            String name,
            Function<Delivery, Mono<Void>> handler
    ) {
        this.transactionManager = transactionManager;
        this.deliveries = deliveries;
        this.name = name;
        this.handler = handler;
    }

    @Nullable
    private Disposable disposable;

    @PostConstruct
    public void start() {
        log.info("Starting message listener '{}'", name);
        disposable = deliveries.get()
                .delayUntil(this::handleDelivery)
                .onErrorResume(e -> {
                    log.error("An unrecoverable error occurred in message listener '{}'", name, e);
                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @PreDestroy
    public void destroy() {
        log.info("Stopping message listener '{}'", name);
        Optional.ofNullable(disposable).ifPresent(Disposable::dispose);
    }

    private Mono<Void> handleDelivery(AcknowledgableDelivery delivery) {
        TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);

        return handler.apply(delivery)
                .as(transactionalOperator::transactional)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(200)))
                .onErrorResume(e -> {
                    delivery.nack(false);

                    String body = new String(delivery.getBody(), UTF_8);
                    log.error("Could not process message '{}' in message listener '{}'", body, name, e);

                    return Mono.empty();
                })
                .doOnSuccess(ignored -> delivery.ack());
    }

}
