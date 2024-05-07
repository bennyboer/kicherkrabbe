package de.bennyboer.kicherkrabbe.eventsourcing.testing;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingConfig;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.messaging.testing.BaseMessagingTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Import(EventSourcingConfig.class)
public class EventListenerTest extends BaseMessagingTest {

    public EventListenerTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    public void send(
            AggregateType aggregateType,
            AggregateId aggregateId,
            Version aggregateVersion,
            EventName eventName,
            Version eventVersion,
            Agent agent,
            Instant date,
            Map<String, Object> event
    ) {
        String exchange = aggregateType.getValue().toLowerCase(Locale.ROOT);
        String routingKey = "events." + eventName.getValue().toLowerCase(Locale.ROOT);
        Map<String, Object> metadata = Map.of(
                "aggregateId", aggregateId.getValue(),
                "aggregateType", aggregateType.getValue(),
                "aggregateVersion", aggregateVersion.getValue(),
                "eventName", eventName.getValue(),
                "eventVersion", eventVersion.getValue(),
                "agentType", agent.getType().name(),
                "agentId", agent.getId().getValue(),
                "date", date,
                "isSnapshot", false
        );
        Map<String, Object> payload = Map.of(
                "event", event,
                "metadata", metadata
        );

        send(exchange, routingKey, payload);
    }

}
