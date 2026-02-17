package de.bennyboer.kicherkrabbe.eventsourcing.example;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Import(TypedEventListenerTest.Config.class)
public class TypedEventListenerTest extends EventListenerTest {

    public record FullEvent(String name, int size) {
    }

    public record SubsetEvent(String name) {
    }

    public record EmptyEvent() {
    }

    interface FullEventHandler {
        Mono<Void> handle(EventMetadata metadata, FullEvent event);
    }

    interface SubsetEventHandler {
        Mono<Void> handle(EventMetadata metadata, SubsetEvent event);
    }

    interface EmptyEventHandler {
        Mono<Void> handle(EventMetadata metadata, EmptyEvent event);
    }

    interface AllEventsHandler {
        Mono<Void> handle(EventMetadata metadata, SubsetEvent event);
    }

    @Configuration
    static class Config {

        @Bean
        public EventListener typedFullEventListener(EventListenerFactory factory, FullEventHandler handler) {
            return factory.createEventListenerForEvent(
                    "test.typed-full",
                    AggregateType.of("SAMPLE"),
                    EventName.of("CREATED"),
                    FullEvent.class,
                    handler::handle
            );
        }

        @Bean
        public EventListener typedSubsetEventListener(EventListenerFactory factory, SubsetEventHandler handler) {
            return factory.createEventListenerForEvent(
                    "test.typed-subset",
                    AggregateType.of("SAMPLE"),
                    EventName.of("SUBSET_TEST"),
                    SubsetEvent.class,
                    handler::handle
            );
        }

        @Bean
        public EventListener typedEmptyEventListener(EventListenerFactory factory, EmptyEventHandler handler) {
            return factory.createEventListenerForEvent(
                    "test.typed-empty",
                    AggregateType.of("SAMPLE"),
                    EventName.of("DELETED"),
                    EmptyEvent.class,
                    handler::handle
            );
        }

        @Bean
        public EventListener typedAllEventsListener(EventListenerFactory factory, AllEventsHandler handler) {
            return factory.createEventListenerForAllEvents(
                    "test.typed-all-events",
                    AggregateType.of("ALL_SAMPLE"),
                    SubsetEvent.class,
                    handler::handle
            );
        }

    }

    @MockitoBean
    FullEventHandler fullEventHandler;

    @MockitoBean
    SubsetEventHandler subsetEventHandler;

    @MockitoBean
    EmptyEventHandler emptyEventHandler;

    @MockitoBean
    AllEventsHandler allEventsHandler;

    @Autowired
    public TypedEventListenerTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setUp() {
        when(fullEventHandler.handle(any(), any())).thenReturn(Mono.empty());
        when(subsetEventHandler.handle(any(), any())).thenReturn(Mono.empty());
        when(emptyEventHandler.handle(any(), any())).thenReturn(Mono.empty());
        when(allEventsHandler.handle(any(), any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldDeserializeFullEventPayload() {
        send(
                AggregateType.of("SAMPLE"),
                AggregateId.of("SAMPLE_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Map.of("name", "Test Pattern", "size", 42)
        );

        verify(fullEventHandler, timeout(5000)).handle(
                argThat(m -> m.getAggregateId().getValue().equals("SAMPLE_ID")
                        && m.getAggregateType().getValue().equals("SAMPLE")),
                argThat(e -> e.name().equals("Test Pattern") && e.size() == 42)
        );
    }

    @Test
    void shouldDeserializeSubsetRecordIgnoringExtraFields() {
        send(
                AggregateType.of("SAMPLE"),
                AggregateId.of("SAMPLE_ID"),
                Version.of(1),
                EventName.of("SUBSET_TEST"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "Subset Test", "size", 99, "extra", "ignored")
        );

        verify(subsetEventHandler, timeout(5000)).handle(
                any(),
                argThat(e -> e.name().equals("Subset Test"))
        );
    }

    @Test
    void shouldDeserializeEmptyRecord() {
        send(
                AggregateType.of("SAMPLE"),
                AggregateId.of("SAMPLE_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(emptyEventHandler, timeout(5000)).handle(
                any(),
                argThat(e -> e != null)
        );
    }

    @Test
    void shouldPassMetadataCorrectly() {
        var date = Instant.parse("2024-06-15T12:30:00Z");

        send(
                AggregateType.of("SAMPLE"),
                AggregateId.of("AGG_123"),
                Version.of(5),
                EventName.of("CREATED"),
                Version.of(2),
                Agent.system(),
                date,
                Map.of("name", "Meta Test", "size", 1)
        );

        verify(fullEventHandler, timeout(5000)).handle(
                argThat(m -> m.getAggregateId().getValue().equals("AGG_123")
                        && m.getAggregateVersion().getValue() == 5
                        && m.getDate().equals(date)),
                any()
        );
    }

    @Test
    void shouldNotCallHandlerWhenPayloadHasIncompatibleType() {
        send(
                AggregateType.of("SAMPLE"),
                AggregateId.of("SAMPLE_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", 123, "size", "not-a-number")
        );

        verify(fullEventHandler, after(2000).never()).handle(any(), any());
    }

    @Test
    void shouldWorkWithAllEventsListener() {
        send(
                AggregateType.of("ALL_SAMPLE"),
                AggregateId.of("ALL_ID"),
                Version.of(1),
                EventName.of("UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "All Events Test")
        );

        verify(allEventsHandler, timeout(5000)).handle(
                argThat(m -> m.getAggregateId().getValue().equals("ALL_ID")),
                argThat(e -> e.name().equals("All Events Test"))
        );
    }

}
