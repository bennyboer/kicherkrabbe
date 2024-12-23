package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.categories.create.CreateCmd;
import de.bennyboer.kicherkrabbe.categories.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.categories.regroup.RegroupCmd;
import de.bennyboer.kicherkrabbe.categories.rename.RenameCmd;
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

public class CategoryService extends AggregateService<Category, CategoryId> {

    public CategoryService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Category.TYPE,
                Category.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<CategoryId>> create(CategoryName name, CategoryGroup group, Agent agent) {
        CategoryId id = CategoryId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name, group))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> rename(CategoryId id, Version version, CategoryName name, Agent agent) {
        return dispatchCommand(id, version, agent, RenameCmd.of(name));
    }

    public Mono<Version> regroup(CategoryId id, Version version, CategoryGroup group, Agent agent) {
        return dispatchCommand(id, version, agent, RegroupCmd.of(group));
    }

    public Mono<Version> delete(CategoryId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateType getAggregateType() {
        return Category.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(CategoryId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Category aggregate) {
        return aggregate.isDeleted();
    }

}
