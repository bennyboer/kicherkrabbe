package de.bennyboer.kicherkrabbe.products.counter;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.products.counter.increment.IncrementCmd;
import de.bennyboer.kicherkrabbe.products.counter.init.InitCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class CounterService extends AggregateService<Counter, CounterId> {

    public CounterService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Counter.TYPE,
                Counter.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<CounterId>> init(CounterId id, Agent agent) {
        return dispatchCommandToLatest(id, agent, InitCmd.of())
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> increment(CounterId id, Version version, Agent agent) {
        return dispatchCommand(
                id,
                version,
                agent,
                IncrementCmd.of()
        );
    }

    @Override
    protected AggregateType getAggregateType() {
        return Counter.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(CounterId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Counter counter) {
        return false;
    }

}
