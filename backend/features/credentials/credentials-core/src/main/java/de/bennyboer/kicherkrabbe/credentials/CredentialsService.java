package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.commons.UserId;
import de.bennyboer.kicherkrabbe.credentials.create.CreateCmd;
import de.bennyboer.kicherkrabbe.credentials.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.credentials.use.InvalidCredentialsUsedOrUserLockedError;
import de.bennyboer.kicherkrabbe.credentials.use.UseCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class CredentialsService extends AggregateService<Credentials, CredentialsId> {

    private final Clock clock;

    public CredentialsService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Credentials.TYPE,
                Credentials.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));

        this.clock = clock;
    }

    public Mono<AggregateIdAndVersion<CredentialsId>> create(Name name, Password password, UserId userId, Agent agent) {
        CredentialsId id = CredentialsId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name, password, userId))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> use(CredentialsId id, Name name, Password password, Agent agent) {
        return dispatchCommandToLatest(id, agent, UseCmd.of(name, password, clock))
                .flatMap(version -> get(id, version))
                .flatMap(credentials -> {
                    if (credentials.hasFailedAttempts()) {
                        return Mono.error(new InvalidCredentialsUsedOrUserLockedError());
                    }

                    return Mono.just(credentials.getVersion());
                });
    }

    public Mono<Version> delete(CredentialsId credentialsId, Version version, Agent agent) {
        return dispatchCommand(credentialsId, version, agent, DeleteCmd.of())
                .flatMap(v -> collapseEvents(credentialsId, v, agent));
    }

    public Mono<Version> delete(CredentialsId credentialsId, Agent agent) {
        return dispatchCommandToLatest(credentialsId, agent, DeleteCmd.of())
                .flatMap(v -> collapseEvents(credentialsId, v, agent));
    }

    @Override
    protected AggregateType getAggregateType() {
        return Credentials.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(CredentialsId credentialsId) {
        return AggregateId.of(credentialsId.getValue());
    }

    @Override
    protected boolean isRemoved(Credentials aggregate) {
        return aggregate.isDeleted();
    }

}
