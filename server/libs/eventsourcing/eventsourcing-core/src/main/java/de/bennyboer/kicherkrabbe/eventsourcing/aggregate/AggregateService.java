package de.bennyboer.kicherkrabbe.eventsourcing.aggregate;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateNotFoundError;
import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public abstract class AggregateService<A extends Aggregate, ID> {

    private final EventSourcingService<A> eventSourcingService;

    public Mono<A> get(ID id) {
        return eventSourcingService.aggregateLatest(toAggregateId(id))
                .filter(this::isNotRemoved);
    }

    public Mono<A> getOrThrow(ID id) {
        return get(id)
                .switchIfEmpty(Mono.error(new AggregateNotFoundError(getAggregateType(), toAggregateId(id))));
    }

    public Mono<A> get(ID id, Version version) {
        return eventSourcingService.aggregate(toAggregateId(id), version)
                .filter(this::isNotRemoved);
    }

    public Mono<A> getOrThrow(ID id, Version version) {
        return get(id, version)
                .switchIfEmpty(Mono.error(new AggregateNotFoundError(getAggregateType(), toAggregateId(id))));
    }

    public Mono<Version> collapseEvents(ID id, Version version, Agent agent) {
        return eventSourcingService.collapseEvents(toAggregateId(id), version, agent);
    }

    protected abstract AggregateType getAggregateType();

    protected abstract AggregateId toAggregateId(ID id);

    protected abstract boolean isRemoved(A aggregate);

    protected Mono<Version> dispatchCommand(ID id, Version version, Agent agent, Command cmd) {
        return eventSourcingService.dispatchCommand(toAggregateId(id), version, cmd, agent);
    }

    protected Mono<Version> dispatchCommandToLatest(ID id, Agent agent, Command cmd) {
        return eventSourcingService.dispatchCommandToLatest(toAggregateId(id), cmd, agent);
    }

    private boolean isNotRemoved(A aggregate) {
        return !isRemoved(aggregate);
    }

}
