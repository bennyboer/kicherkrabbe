package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.patterns.create.CreateCmd;
import de.bennyboer.kicherkrabbe.patterns.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.patterns.delete.category.RemoveCategoryCmd;
import de.bennyboer.kicherkrabbe.patterns.publish.PublishCmd;
import de.bennyboer.kicherkrabbe.patterns.rename.RenameCmd;
import de.bennyboer.kicherkrabbe.patterns.unpublish.UnpublishCmd;
import de.bennyboer.kicherkrabbe.patterns.update.attribution.UpdateAttributionCmd;
import de.bennyboer.kicherkrabbe.patterns.update.categories.UpdateCategoriesCmd;
import de.bennyboer.kicherkrabbe.patterns.update.description.UpdateDescriptionCmd;
import de.bennyboer.kicherkrabbe.patterns.update.extras.UpdateExtrasCmd;
import de.bennyboer.kicherkrabbe.patterns.update.images.UpdateImagesCmd;
import de.bennyboer.kicherkrabbe.patterns.update.number.UpdateNumberCmd;
import de.bennyboer.kicherkrabbe.patterns.update.variants.UpdateVariantsCmd;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;
import java.util.Set;

public class PatternService extends AggregateService<Pattern, PatternId> {

    public PatternService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Pattern.TYPE,
                Pattern.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<PatternId>> create(
            PatternName name,
            PatternNumber number,
            @Nullable PatternDescription description,
            PatternAttribution attribution,
            Set<PatternCategoryId> categories,
            List<ImageId> images,
            List<PatternVariant> variants,
            List<PatternExtra> extras,
            Agent agent
    ) {
        var id = PatternId.create();
        var cmd = CreateCmd.of(
                name,
                number,
                description,
                attribution,
                categories,
                images,
                variants,
                extras
        );

        return dispatchCommandToLatest(id, agent, cmd)
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> publish(PatternId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, PublishCmd.of());
    }

    public Mono<Version> unpublish(PatternId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, UnpublishCmd.of());
    }

    public Mono<Version> rename(PatternId id, Version version, PatternName name, Agent agent) {
        return dispatchCommand(id, version, agent, RenameCmd.of(name));
    }

    public Mono<Version> updateNumber(PatternId id, Version version, PatternNumber number, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateNumberCmd.of(number));
    }

    public Mono<Version> updateAttribution(PatternId id, Version version, PatternAttribution attribution, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateAttributionCmd.of(attribution));
    }

    public Mono<Version> updateCategories(
            PatternId id,
            Version version,
            Set<PatternCategoryId> categories,
            Agent agent
    ) {
        return dispatchCommand(id, version, agent, UpdateCategoriesCmd.of(categories));
    }

    public Mono<Version> updateImages(PatternId id, Version version, List<ImageId> images, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateImagesCmd.of(images));
    }

    public Mono<Version> updateVariants(PatternId id, Version version, List<PatternVariant> variants, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateVariantsCmd.of(variants));
    }

    public Mono<Version> updateExtras(PatternId id, Version version, List<PatternExtra> extras, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateExtrasCmd.of(extras));
    }

    public Mono<Version> updateDescription(
            PatternId id,
            Version version,
            @Nullable PatternDescription description,
            Agent agent
    ) {
        return dispatchCommand(id, version, agent, UpdateDescriptionCmd.of(description));
    }

    public Mono<Version> removeCategory(PatternId id, Version version, PatternCategoryId categoryId, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveCategoryCmd.of(categoryId));
    }

    public Mono<Version> delete(PatternId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateType getAggregateType() {
        return Pattern.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(PatternId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Pattern aggregate) {
        return aggregate.isDeleted();
    }

}
