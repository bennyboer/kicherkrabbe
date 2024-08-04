package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.changes.ReceiverId;
import de.bennyboer.kicherkrabbe.changes.ResourceChange;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.http.api.*;
import de.bennyboer.kicherkrabbe.patterns.persistence.categories.PatternCategoryRepo;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPattern;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.PatternLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.patterns.Actions.*;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@AllArgsConstructor
public class PatternsModule {

    private final PatternService patternService;

    private final PermissionsService permissionsService;

    private final PatternLookupRepo patternLookupRepo;

    private final ResourceChangesTracker changesTracker;

    private final PatternCategoryRepo patternCategoryRepo;

    public Mono<PatternsPage> getPatterns(
            String searchTerm,
            Set<String> categories,
            long skip,
            long limit,
            Agent agent
    ) {
        Set<PatternCategoryId> internalCategories = toInternalCategories(categories);

        return getAccessiblePatternIds(agent)
                .collectList()
                .flatMap(patternIds -> patternLookupRepo.find(patternIds, internalCategories, searchTerm, skip, limit))
                .map(result -> PatternsPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults()
                                .stream()
                                .map(pattern -> PatternDetails.of(
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
                                )).toList()
                ));
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
        notNull(name, "Pattern name must be given");
        check(!name.isBlank(), "Pattern name must not be blank");
        notNull(attribution, "Attribution must be given");
        notNull(categories, "Categories must be given");
        notNull(images, "Images must be given");
        check(!images.isEmpty(), "At least one image must be given");
        notNull(variants, "Variants must be given");
        check(!variants.isEmpty(), "At least one variant must be given");
        notNull(extras, "Extras must be given");

        var internalName = PatternName.of(name);
        var internalAttribution = toInternalAttribution(attribution);
        Set<PatternCategoryId> internalCategories = toInternalCategories(categories);
        var internalImages = toInternalImages(images);
        var internalVariants = toInternalVariants(variants);
        var internalExtras = toInternalExtras(extras);

        return assertAgentIsAllowedTo(agent, CREATE)
                .then(assertCategoriesAvailable(internalCategories))
                .then(patternService.create(
                        internalName,
                        internalAttribution,
                        internalCategories,
                        internalImages,
                        internalVariants,
                        internalExtras,
                        agent
                ))
                .map(result -> result.getId().getValue());
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

    @Transactional(propagation = MANDATORY)
    public Flux<String> removeCategoryFromPatterns(String categoryId, Agent agent) {
        return Flux.empty(); // TODO
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

    public Mono<Void> removePermissionsOnPattern(String patternId) {
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

    public Mono<Void> markCategoryAsAvailable(String id, String name) {
        var category = PatternCategory.of(PatternCategoryId.of(id), PatternCategoryName.of(name));

        return patternCategoryRepo.save(category).then();
    }

    public Mono<Void> markCategoryAsUnavailable(String id) {
        return patternCategoryRepo.removeById(PatternCategoryId.of(id));
    }

    public Mono<Void> allowAnonymousAndSystemUsersToReadPublishedPattern(String patternId) {
        return Mono.empty(); // TODO
    }

    public Mono<Void> disallowAnonymousAndSystemUsersToReadPublishedPattern(String patternId) {
        return Mono.empty(); // TODO
    }

    private Flux<PatternId> getAccessiblePatternIds(Agent agent) {
        Holder holder = toHolder(agent);
        ResourceType resourceType = getResourceType();

        return permissionsService.findPermissionsByHolderAndResourceType(holder, resourceType)
                .mapNotNull(permission -> permission.getResource()
                        .getId()
                        .map(id -> PatternId.of(id.getValue()))
                        .orElse(null));
    }

    private Mono<Void> assertCategoriesAvailable(Set<PatternCategoryId> categories) {
        return patternCategoryRepo.findByIds(categories)
                .map(PatternCategory::getId)
                .collect(Collectors.toSet())
                .flatMap(foundIds -> {
                    if (foundIds.equals(categories)) {
                        return Mono.empty();
                    }

                    Set<PatternCategoryId> missingCategories = new HashSet<>(categories);
                    missingCategories.removeAll(foundIds);

                    return Mono.error(new CategoriesMissingError(missingCategories));
                });
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable PatternId patternId) {
        Permission permission = toPermission(agent, action, patternId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable PatternId patternId) {
        Holder holder = toHolder(agent);
        var resourceType = getResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(patternId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(patternId.getValue()))))
                .orElseGet(() -> permissionBuilder.onType(resourceType));
    }

    private Holder toHolder(Agent agent) {
        if (agent.isSystem()) {
            return Holder.group(HolderId.system());
        } else if (agent.isAnonymous()) {
            return Holder.group(HolderId.anonymous());
        } else {
            return Holder.user(HolderId.of(agent.getId().getValue()));
        }
    }

    private ResourceType getResourceType() {
        return ResourceType.of("PATTERN");
    }

    private PatternAttribution toInternalAttribution(PatternAttributionDTO attribution) {
        notNull(attribution, "Attribution must be given");

        OriginalPatternName originalPatternName = Optional.ofNullable(attribution.originalPatternName).map(
                OriginalPatternName::of).orElse(null);
        PatternDesigner designer = Optional.ofNullable(attribution.designer).map(PatternDesigner::of).orElse(null);

        return PatternAttribution.of(originalPatternName, designer);
    }

    private Set<PatternCategoryId> toInternalCategories(Set<String> categories) {
        notNull(categories, "Categories must be given");

        return categories.stream()
                .map(PatternCategoryId::of)
                .collect(Collectors.toSet());
    }

    private List<ImageId> toInternalImages(List<String> images) {
        notNull(images, "Images must be given");

        return images.stream()
                .map(ImageId::of)
                .toList();
    }

    private List<PatternVariant> toInternalVariants(List<PatternVariantDTO> variants) {
        notNull(variants, "Variants must be given");

        return variants.stream()
                .map(this::toInternalVariant)
                .toList();
    }

    private PatternVariant toInternalVariant(PatternVariantDTO variant) {
        notNull(variant.pricedSizeRanges, "Priced size ranges must be given");

        PatternVariantName name = PatternVariantName.of(variant.name);
        Set<PricedSizeRange> pricedSizeRanges = toInternalPriceSizeRanges(variant.pricedSizeRanges);

        return PatternVariant.of(name, pricedSizeRanges);
    }

    private Set<PricedSizeRange> toInternalPriceSizeRanges(Set<PricedSizeRangeDTO> pricedSizeRanges) {
        notNull(pricedSizeRanges, "Priced size ranges must be given");

        return pricedSizeRanges.stream()
                .map(this::toInternalPriceSizeRange)
                .collect(Collectors.toSet());
    }

    private PricedSizeRange toInternalPriceSizeRange(PricedSizeRangeDTO pricedSizeRange) {
        notNull(pricedSizeRange, "Priced size range must be given");
        notNull(pricedSizeRange.price, "Price must be given");

        long from = pricedSizeRange.from;
        Long to = pricedSizeRange.to;
        String unit = pricedSizeRange.unit;
        Money price = toInternalMoney(pricedSizeRange.price);

        return PricedSizeRange.of(
                from,
                to,
                unit,
                price
        );
    }

    private Money toInternalMoney(MoneyDTO money) {
        notNull(money, "Money must be given");
        notNull(money.currency, "Currency must be given");

        return switch (money.currency) {
            case "EUR" -> Money.euro(money.amount);
            default -> throw new IllegalArgumentException("Unsupported currency: " + money.currency);
        };
    }

    private List<PatternExtra> toInternalExtras(List<PatternExtraDTO> extras) {
        notNull(extras, "Extras must be given");

        return extras.stream()
                .map(this::toInternalExtra)
                .toList();
    }

    private PatternExtra toInternalExtra(PatternExtraDTO extra) {
        notNull(extra, "Extra must be given");
        notNull(extra.name, "Extra name must be given");
        notNull(extra.price, "Extra price must be given");

        PatternExtraName name = PatternExtraName.of(extra.name);
        Money price = toInternalMoney(extra.price);

        return PatternExtra.of(name, price);
    }

}
