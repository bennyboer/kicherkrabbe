package de.bennyboer.kicherkrabbe.messaging.listener;

import de.bennyboer.kicherkrabbe.messaging.inbox.IncomingMessageId;
import de.bennyboer.kicherkrabbe.messaging.inbox.MessagingInbox;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.IncomingMessageAlreadySeenException;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class MessageListener {

    private final ReactiveTransactionManager transactionManager;

    private final MessagingInbox inbox;

    private final Supplier<Flux<AcknowledgableMessage>> deliveries;

    private final String name;

    private final Function<Message, Mono<Void>> handler;

    @Nullable
    private Disposable disposable;

    public MessageListener(
            ReactiveTransactionManager transactionManager,
            MessagingInbox inbox,
            Supplier<Flux<AcknowledgableMessage>> deliveries,
            String name,
            Function<Message, Mono<Void>> handler
    ) {
        this.transactionManager = transactionManager;
        this.inbox = inbox;
        this.name = name;
        this.handler = handler;
        this.deliveries = deliveries;
    }

    @PostConstruct
    public void start() {
        log.info("Starting message listener '{}'", name);
        disposable = Flux.defer(deliveries::get)
                .delayUntil(this::handleDelivery)
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofMinutes(5))
                        .doBeforeRetry(signal -> log.warn(
                                "Retrying message listener '{}' after error (attempt {})",
                                name,
                                signal.totalRetries() + 1,
                                signal.failure()
                        )))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @PreDestroy
    public void destroy() {
        log.info("Stopping message listener '{}'", name);
        Optional.ofNullable(disposable).ifPresent(Disposable::dispose);
    }

    private Mono<Void> handleDelivery(AcknowledgableMessage delivery) {
        var transactionalOperator = TransactionalOperator.create(transactionManager);
        Message message = delivery.getMessage();
        var incomingMessageId = IncomingMessageId.of(name + message.getMessageProperties().getMessageId());

        var body = new String(message.getBody(), UTF_8);

        return inbox.addMessage(incomingMessageId)
                .then(Mono.defer(() -> handler.apply(message)))
                .as(transactionalOperator::transactional)
                .onErrorResume(IncomingMessageAlreadySeenException.class, e -> {
                    log.warn(
                            "Message '{}' was already seen in message listener '{}'. Ignoring...",
                            incomingMessageId,
                            name
                    );
                    return Mono.empty();
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(200)))
                .then(Mono.defer(delivery::ack))
                .onErrorResume(e -> {
                    log.error("Could not process message '{}' in message listener '{}'", body, name, e);
                    return delivery.nack(false);
                });
    }

}
