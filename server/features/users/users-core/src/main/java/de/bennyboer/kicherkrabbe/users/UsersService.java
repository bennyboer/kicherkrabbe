package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.users.create.CreateCmd;
import de.bennyboer.kicherkrabbe.users.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.users.rename.RenameCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class UsersService extends AggregateService<User, UserId> {

    public UsersService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                User.TYPE,
                User.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<UserId>> create(FullName name, Mail mail, Agent agent) {
        UserId id = UserId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name, mail))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> rename(UserId userId, Version version, FullName name, Agent agent) {
        return dispatchCommand(userId, version, agent, RenameCmd.of(name));
    }

    public Mono<Version> delete(UserId userId, Version version, Agent agent) {
        return dispatchCommand(userId, version, agent, DeleteCmd.of())
                .flatMap(v -> collapseEvents(userId, v, agent));
    }

    @Override
    protected AggregateType getAggregateType() {
        return User.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(UserId userId) {
        return AggregateId.of(userId.getValue());
    }

    @Override
    protected boolean isRemoved(User aggregate) {
        return aggregate.isDeleted();
    }

}
