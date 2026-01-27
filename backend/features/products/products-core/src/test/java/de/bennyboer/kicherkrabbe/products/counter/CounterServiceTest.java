package de.bennyboer.kicherkrabbe.products.counter;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CounterServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final CounterService counterService = new CounterService(
            repo,
            eventPublisher,
            Clock.systemUTC()
    );

    @Test
    void shouldInitCounter() {
        // when: initializing a counter
        var id = init(CounterId.of("TEST_ID"));

        // then: the counter is initialized
        var counter = get(id);
        assertThat(counter.getId()).isEqualTo(id);
        assertThat(counter.getVersion()).isEqualTo(Version.zero());
        assertThat(counter.getValue()).isEqualTo(0L);
    }

    @Test
    void shouldIncrementCounter() {
        // given: a counter
        var id = init(CounterId.of("TEST_ID"));

        // when: incrementing the counter
        var version = increment(id, Version.zero());

        // then: the counter is incremented
        var counter = get(id);
        assertThat(counter.getId()).isEqualTo(id);
        assertThat(counter.getVersion()).isEqualTo(version);
        assertThat(counter.getValue()).isEqualTo(1L);

        // when: incrementing the counter again
        version = increment(id, version);

        // then: the counter is incremented again
        counter = get(id);
        assertThat(counter.getId()).isEqualTo(id);
        assertThat(counter.getVersion()).isEqualTo(version);
        assertThat(counter.getValue()).isEqualTo(2L);
    }

    @Test
    void shouldNotIncrementCounterGivenAnOutdatedVersion() {
        // given: a counter
        var id = init(CounterId.of("TEST_ID"));
        increment(id, Version.zero());

        // when: incrementing the counter with an outdated version; then: an error is thrown
        assertThatThrownBy(() -> increment(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a counter
        var id = init(CounterId.of("TEST_ID"));

        // when: incrementing the counter 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = increment(id, version);
        }

        // then: there are 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Counter.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private CounterId init(CounterId id) {
        return counterService.init(id, Agent.system()).block().getId();
    }

    private Counter get(CounterId id) {
        return counterService.get(id).block();
    }

    private Version increment(CounterId id, Version version) {
        return counterService.increment(id, version, Agent.system()).block();
    }

}
