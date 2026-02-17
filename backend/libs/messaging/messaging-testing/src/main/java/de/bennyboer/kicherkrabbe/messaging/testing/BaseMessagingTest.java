package de.bennyboer.kicherkrabbe.messaging.testing;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTarget;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.scheduler.Schedulers;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;

@MessagingTest
public abstract class BaseMessagingTest {

    private final MessagingOutbox outbox;

    private final ReactiveTransactionManager transactionManager;

    @Nullable
    @Autowired(required = false)
    private InMemoryMessageBus messageBus;

    public BaseMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        this.outbox = outbox;
        this.transactionManager = transactionManager;
    }

    public void send(String exchange, String routingKey, Map<String, Object> payload) {
        var exchangeTarget = ExchangeTarget.of(exchange);
        var messageTarget = MessageTarget.exchange(exchangeTarget);
        var rKey = RoutingKey.parse(routingKey);
        var entry = MessagingOutboxEntry.create(
                messageTarget,
                rKey,
                payload,
                Clock.systemUTC()
        );
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        if (messageBus != null) {
            outbox.insert(entry)
                    .as(transactionalOperator::transactional)
                    .then(outbox.publishNextUnpublishedEntries())
                    .subscribeOn(Schedulers.boundedElastic())
                    .block(Duration.ofSeconds(5));

            messageBus.awaitIdle(Duration.ofSeconds(5));
        } else {
            outbox.insert(entry)
                    .as(transactionalOperator::transactional)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
        }
    }

}
