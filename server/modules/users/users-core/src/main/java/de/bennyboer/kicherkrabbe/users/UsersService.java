package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.users.create.CreateCmd;
import de.bennyboer.kicherkrabbe.users.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.users.rename.RenameCmd;
import reactor.core.publisher.Mono;

import java.util.List;

public class UsersService extends AggregateService<User, UserId> {

    public UsersService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                User.TYPE,
                User.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    public Mono<AggregateIdAndVersion<UserId>> create(FullName name, Mail mail, Agent agent) {
        UserId id = UserId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name, mail))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> rename(UserId userId, FullName name, Agent agent) {
        return dispatchCommandToLatest(userId, agent, RenameCmd.of(name));
    }

    public Mono<Version> delete(UserId userId, Agent agent) {
        return dispatchCommandToLatest(userId, agent, DeleteCmd.of())
                .flatMap(version -> collapseEvents(userId, version, agent));
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
