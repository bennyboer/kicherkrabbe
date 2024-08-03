package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.changes.ReceiverId;
import de.bennyboer.kicherkrabbe.changes.ResourceChange;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternExtraDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternVariantDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternsSortDTO;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPattern;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.PatternLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.*;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.patterns.Actions.*;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@AllArgsConstructor
public class PatternsModule {

    private final PatternService patternService;

    private final PermissionsService permissionsService;

    private final PatternLookupRepo patternLookupRepo;

    private final ResourceChangesTracker changesTracker;

    public Mono<PatternsPage> getPatterns(
            String searchTerm,
            Set<String> categories,
            long skip,
            long limit,
            Agent agent
    ) {
        return Mono.empty(); // TODO
    }

    public Mono<PatternDetails> getPattern(String patternId, Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<PublishedPatternsPage> getPublishedPatterns(
            String searchTerm,
            Set<String> categories,
            PatternsSortDTO sort,
            long skip,
            long limit,
            Agent agent
    ) {
        return Mono.empty(); // TODO
    }

    public Mono<PublishedPattern> getPublishedPattern(String patternId, Agent agent) {
        return Mono.empty(); // TODO
    }

    public Flux<PatternCategory> getAvailableCategoriesForPatterns(Agent agent) {
        return Flux.empty(); // TODO
    }

    public Flux<PatternCategory> getCategoriesUsedInPatterns(Agent agent) {
        return Flux.empty(); // TODO
    }

    public Flux<ResourceChange> getPatternChanges(Agent agent) {
        var receiverId = ReceiverId.of(agent.getId().getValue());

        return changesTracker.getChanges(receiverId);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<String> createPattern(
            String name,
            PatternAttributionDTO attribution,
            Set<String> categories,
            List<String> images,
            List<PatternVariantDTO> variants,
            List<PatternExtraDTO> extras,
            Agent agent
    ) {
        return Mono.empty(); // TODO
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> renamePattern(String patternId, long version, String name, Agent agent) {
        return Mono.empty(); // TODO
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> publishPattern(String patternId, long version, Agent agent) {
        return Mono.empty(); // TODO
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> unpublishPattern(String patternId, long version, Agent agent) {
        return Mono.empty(); // TODO
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updatePatternVariants(
            String patternId,
            long version,
            List<PatternVariantDTO> variants,
            Agent agent
    ) {
        return Mono.empty(); // TODO
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updatePatternAttribution(
            String patternId,
            long version,
            PatternAttributionDTO attribution,
            Agent agent
    ) {
        return Mono.empty(); // TODO
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updatePatternCategories(String patternId, long version, Set<String> categories, Agent agent) {
        return Mono.empty(); // TODO
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updatePatternImages(String patternId, long version, List<String> images, Agent agent) {
        return Mono.empty(); // TODO
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updatePatternExtras(String patternId, long version, List<PatternExtraDTO> extras, Agent agent) {
        return Mono.empty(); // TODO
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Void> deletePattern(String patternId, long version, Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<Void> allowUserToCreatePatterns(String userId) {
        var holder = Holder.user(HolderId.of(userId));

        var createPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(CREATE)
                .onType(getResourceType());

        return permissionsService.addPermission(createPermission);
    }

    public Mono<Void> allowUserToManagePattern(String patternId, String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resource = Resource.of(getResourceType(), ResourceId.of(patternId));

        var readPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .on(resource);
        var renamePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(RENAME)
                .on(resource);
        var updateAttributionPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_ATTRIBUTION)
                .on(resource);
        var updateCategoriesPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_CATEGORIES)
                .on(resource);
        var updateImagesPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_IMAGES)
                .on(resource);
        var updateVariantsPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_VARIANTS)
                .on(resource);
        var updateExtrasPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_EXTRAS)
                .on(resource);
        var deletePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermissions(
                readPermission,
                renamePermission,
                updateAttributionPermission,
                updateCategoriesPermission,
                updateImagesPermission,
                updateVariantsPermission,
                updateExtrasPermission,
                deletePermission
        );
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        var holder = Holder.user(HolderId.of(userId));

        return permissionsService.removePermissionsByHolder(holder);
    }

    public Mono<Void> removePermissionsForPattern(String patternId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(patternId));

        return permissionsService.removePermissionsByResource(resource);
    }

    public Mono<Void> updatePatternInLookup(String patternId) {
        return patternService.getOrThrow(PatternId.of(patternId))
                .flatMap(pattern -> patternLookupRepo.update(LookupPattern.of(
                        pattern.getId(),
                        pattern.getVersion(),
                        pattern.isPublished(),
                        pattern.getName(),
                        pattern.getAttribution(),
                        pattern.getCategories(),
                        pattern.getImages(),
                        pattern.getVariants(),
                        pattern.getExtras(),
                        pattern.getCreatedAt()
                )))
                .then();
    }

    public Mono<Void> removePatternFromLookup(String patternId) {
        return patternLookupRepo.remove(PatternId.of(patternId));
    }

    private ResourceType getResourceType() {
        return ResourceType.of("PATTERN");
    }

}
