package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.changes.ReceiverId;
import de.bennyboer.kicherkrabbe.changes.ResourceChange;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.api.*;
import de.bennyboer.kicherkrabbe.offers.persistence.categories.OfferCategoryRepo;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.*;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.ProductForOfferLookupRepo;
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

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.offers.Actions.*;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@AllArgsConstructor
public class OffersModule {

    private final OfferService offerService;

    private final PermissionsService permissionsService;

    private final OfferLookupRepo offerLookupRepo;

    private final ProductForOfferLookupRepo productForOfferLookupRepo;

    private final OfferCategoryRepo offerCategoryRepo;

    private final ResourceChangesTracker changesTracker;

    public Flux<ResourceChange> getOfferChanges(Agent agent) {
        var receiverId = ReceiverId.of(agent.getId().getValue());
        return changesTracker.getChanges(receiverId);
    }

    public Mono<OfferDetails> getOffer(String offerId, Agent agent) {
        var id = OfferId.of(offerId);

        return assertAgentIsAllowedTo(agent, READ, id)
                .then(offerService.getOrThrow(id))
                .flatMap(offer -> resolveProductData(offer.getProductId())
                        .map(product -> toOfferDetails(offer, product)));
    }

    public Mono<OffersPage> getOffers(String searchTerm, long skip, long limit, Agent agent) {
        return getAccessibleOfferIds(agent)
                .collectList()
                .flatMap(offerIds -> offerLookupRepo.find(offerIds, searchTerm, skip, limit))
                .map(result -> OffersPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults()
                                .stream()
                                .map(this::toLookupOfferDetails)
                                .toList()
                ));
    }

    public Mono<PublishedOffersPage> getPublishedOffers(
            String searchTerm,
            @Nullable Set<String> categories,
            @Nullable Set<String> sizes,
            @Nullable PriceRangeDTO priceRange,
            @Nullable OffersSortDTO sort,
            long skip,
            long limit,
            Agent ignoredAgent
    ) {
        var categoryIds = categories != null
                ? categories.stream().map(OfferCategoryId::of).collect(Collectors.toSet())
                : Set.<OfferCategoryId>of();
        var offerSizes = sizes != null
                ? sizes.stream().map(OfferSize::of).collect(Collectors.toSet())
                : Set.<OfferSize>of();
        @Nullable Long minPrice = priceRange != null ? priceRange.minPrice : null;
        @Nullable Long maxPrice = priceRange != null ? priceRange.maxPrice : null;
        @Nullable OfferSortProperty sortProperty = sort != null && sort.property != null ? toSortProperty(sort.property) : null;
        @Nullable OfferSortDirection sortDirection = sort != null && sort.direction != null ? toSortDirection(sort.direction) : null;

        var query = PublishedOfferQuery.of(searchTerm, categoryIds, offerSizes, minPrice, maxPrice, sortProperty, sortDirection, skip, limit);

        return offerLookupRepo.findPublished(query)
                .map(result -> PublishedOffersPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults()
                                .stream()
                                .map(this::toPublishedOffer)
                                .toList()
                ));
    }

    public Mono<ProductsPage> getProductsForOfferCreation(String searchTerm, long skip, long limit, Agent agent) {
        return assertAgentIsAllowedTo(agent, CREATE)
                .then(productForOfferLookupRepo.findAll(searchTerm, skip, limit))
                .map(result -> ProductsPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults()
                ));
    }

    public Mono<PublishedOffer> getPublishedOffer(String offerIdOrAlias, Agent agent) {
        return offerLookupRepo.findPublishedByAlias(OfferAlias.of(offerIdOrAlias))
                .switchIfEmpty(Mono.defer(() -> offerLookupRepo.findPublished(OfferId.of(offerIdOrAlias))))
                .flatMap(offer -> assertAgentIsAllowedTo(agent, READ_PUBLISHED, offer.getId())
                        .thenReturn(offer))
                .map(this::toPublishedOffer)
                .onErrorResume(MissingPermissionError.class, ignored -> Mono.empty());
    }

    public Flux<OfferCategory> getAvailableCategoriesForOffers(Agent ignoredAgent) {
        return offerCategoryRepo.findAll();
    }

    public Flux<String> getAvailableSizesForOffers(Agent ignoredAgent) {
        return offerLookupRepo.findDistinctPublishedSizes();
    }

    @Transactional(propagation = MANDATORY)
    public Mono<String> createOffer(
            String title,
            String size,
            Set<String> categoryIds,
            String productId,
            List<String> imageIds,
            NotesDTO notesDTO,
            MoneyDTO priceDTO,
            Agent agent
    ) {
        notNull(title, "Title must be given");
        notNull(size, "Size must be given");
        notNull(categoryIds, "Category IDs must be given");
        notNull(productId, "Product ID must be given");
        notNull(imageIds, "Image IDs must be given");
        notNull(notesDTO, "Notes must be given");
        notNull(priceDTO, "Price must be given");

        var offerTitle = OfferTitle.of(title);
        var offerSize = OfferSize.of(size);
        var categories = categoryIds.stream().map(OfferCategoryId::of).collect(Collectors.toSet());
        var images = imageIds.stream().map(ImageId::of).toList();
        var notes = toNotes(notesDTO);
        var price = Money.of(priceDTO.amount, Currency.fromShortForm(priceDTO.currency));

        var alias = OfferAlias.fromTitle(offerTitle);

        return assertAgentIsAllowedTo(agent, CREATE)
                .then(assertAliasIsNotAlreadyInUse(alias, null))
                .then(offerService.create(
                        offerTitle,
                        offerSize,
                        categories,
                        ProductId.of(productId),
                        images,
                        notes,
                        price,
                        agent
                ))
                .map(result -> result.getId().getValue());
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Void> deleteOffer(String offerId, long version, Agent agent) {
        var id = OfferId.of(offerId);

        return assertAgentIsAllowedTo(agent, DELETE, id)
                .then(offerService.delete(id, Version.of(version), agent))
                .then();
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> publishOffer(String offerId, long version, Agent agent) {
        var id = OfferId.of(offerId);

        return assertAgentIsAllowedTo(agent, PUBLISH, id)
                .then(offerService.publish(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> unpublishOffer(String offerId, long version, Agent agent) {
        var id = OfferId.of(offerId);

        return assertAgentIsAllowedTo(agent, UNPUBLISH, id)
                .then(offerService.unpublish(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> reserveOffer(String offerId, long version, Agent agent) {
        var id = OfferId.of(offerId);

        return assertAgentIsAllowedTo(agent, RESERVE, id)
                .then(offerService.reserve(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> unreserveOffer(String offerId, long version, Agent agent) {
        var id = OfferId.of(offerId);

        return assertAgentIsAllowedTo(agent, UNRESERVE, id)
                .then(offerService.unreserve(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> archiveOffer(String offerId, long version, Agent agent) {
        var id = OfferId.of(offerId);

        return assertAgentIsAllowedTo(agent, ARCHIVE, id)
                .then(offerService.archive(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateOfferImages(String offerId, long version, List<String> imageIds, Agent agent) {
        var id = OfferId.of(offerId);
        var images = imageIds.stream().map(ImageId::of).toList();

        return assertAgentIsAllowedTo(agent, UPDATE_IMAGES, id)
                .then(offerService.updateImages(id, Version.of(version), images, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateOfferNotes(String offerId, long version, NotesDTO notesDTO, Agent agent) {
        var id = OfferId.of(offerId);
        var notes = toNotes(notesDTO);

        return assertAgentIsAllowedTo(agent, UPDATE_NOTES, id)
                .then(offerService.updateNotes(id, Version.of(version), notes, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateOfferPrice(String offerId, long version, MoneyDTO priceDTO, Agent agent) {
        var id = OfferId.of(offerId);
        var price = Money.of(priceDTO.amount, Currency.fromShortForm(priceDTO.currency));

        return assertAgentIsAllowedTo(agent, UPDATE_PRICE, id)
                .then(offerService.updatePrice(id, Version.of(version), price, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> addOfferDiscount(String offerId, long version, MoneyDTO discountedPriceDTO, Agent agent) {
        var id = OfferId.of(offerId);
        var discountedPrice = Money.of(discountedPriceDTO.amount, Currency.fromShortForm(discountedPriceDTO.currency));

        return assertAgentIsAllowedTo(agent, ADD_DISCOUNT, id)
                .then(offerService.addDiscount(id, Version.of(version), discountedPrice, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> removeOfferDiscount(String offerId, long version, Agent agent) {
        var id = OfferId.of(offerId);

        return assertAgentIsAllowedTo(agent, REMOVE_DISCOUNT, id)
                .then(offerService.removeDiscount(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateOfferTitle(String offerId, long version, String title, Agent agent) {
        var id = OfferId.of(offerId);
        var offerTitle = OfferTitle.of(title);
        var alias = OfferAlias.fromTitle(offerTitle);

        return assertAgentIsAllowedTo(agent, UPDATE_TITLE, id)
                .then(assertAliasIsNotAlreadyInUse(alias, id))
                .then(offerService.updateTitle(id, Version.of(version), offerTitle, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateOfferSize(String offerId, long version, String size, Agent agent) {
        var id = OfferId.of(offerId);
        var offerSize = OfferSize.of(size);

        return assertAgentIsAllowedTo(agent, UPDATE_SIZE, id)
                .then(offerService.updateSize(id, Version.of(version), offerSize, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateOfferCategories(String offerId, long version, Set<String> categoryIds, Agent agent) {
        var id = OfferId.of(offerId);
        var categories = categoryIds.stream().map(OfferCategoryId::of).collect(Collectors.toSet());

        return assertAgentIsAllowedTo(agent, UPDATE_CATEGORIES, id)
                .then(offerService.updateCategories(id, Version.of(version), categories, agent))
                .map(Version::getValue);
    }

    public Mono<Void> markCategoryAsAvailable(String id, String name) {
        var category = OfferCategory.of(OfferCategoryId.of(id), OfferCategoryName.of(name));

        return offerCategoryRepo.save(category).then();
    }

    public Mono<Void> renameCategoryIfAvailable(String id, String name) {
        var categoryId = OfferCategoryId.of(id);

        return offerCategoryRepo.findById(categoryId)
                .flatMap(ignored -> {
                    var updated = OfferCategory.of(categoryId, OfferCategoryName.of(name));
                    return offerCategoryRepo.save(updated).then();
                });
    }

    public Mono<Void> markCategoryAsUnavailable(String id) {
        return offerCategoryRepo.removeById(OfferCategoryId.of(id));
    }

    @Transactional(propagation = MANDATORY)
    public Flux<String> removeCategoryFromOffers(String categoryId, Agent agent) {
        return offerLookupRepo.findByCategoryId(OfferCategoryId.of(categoryId))
                .delayUntil(offer -> offerService.removeCategory(
                        offer.getId(),
                        offer.getVersion(),
                        OfferCategoryId.of(categoryId),
                        agent
                ))
                .map(offer -> offer.getId().getValue());
    }

    public Mono<Void> allowUserToCreateOffers(String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resourceType = getResourceType();

        var createPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(CREATE)
                .onType(resourceType);

        return permissionsService.addPermission(createPermission);
    }

    public Mono<Void> allowUserToManageOffer(String offerId, String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resource = Resource.of(getResourceType(), ResourceId.of(offerId));

        var readPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .on(resource);
        var readPublishedPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);
        var publishPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(PUBLISH)
                .on(resource);
        var unpublishPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UNPUBLISH)
                .on(resource);
        var reservePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(RESERVE)
                .on(resource);
        var unreservePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UNRESERVE)
                .on(resource);
        var archivePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(ARCHIVE)
                .on(resource);
        var updateImagesPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_IMAGES)
                .on(resource);
        var updateNotesPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_NOTES)
                .on(resource);
        var updatePricePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_PRICE)
                .on(resource);
        var addDiscountPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(ADD_DISCOUNT)
                .on(resource);
        var removeDiscountPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(REMOVE_DISCOUNT)
                .on(resource);
        var updateTitlePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_TITLE)
                .on(resource);
        var updateSizePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_SIZE)
                .on(resource);
        var updateCategoriesPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_CATEGORIES)
                .on(resource);
        var deletePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermissions(
                readPermission,
                readPublishedPermission,
                publishPermission,
                unpublishPermission,
                reservePermission,
                unreservePermission,
                archivePermission,
                updateImagesPermission,
                updateNotesPermission,
                updatePricePermission,
                addDiscountPermission,
                removeDiscountPermission,
                updateTitlePermission,
                updateSizePermission,
                updateCategoriesPermission,
                deletePermission
        );
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        var holder = Holder.user(HolderId.of(userId));

        return permissionsService.removePermissionsByHolder(holder);
    }

    public Mono<Void> removePermissionsOnOffer(String offerId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(offerId));

        return permissionsService.removePermissionsByResource(resource);
    }

    public Mono<Void> allowAnonymousAndSystemUsersToReadPublishedOffer(String offerId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(offerId));
        var systemHolder = Holder.group(HolderId.system());
        var anonymousHolder = Holder.group(HolderId.anonymous());

        var readPublishedPermission = Permission.builder()
                .holder(anonymousHolder)
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);
        var readPublishedSystemPermission = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);

        return permissionsService.addPermissions(
                readPublishedPermission,
                readPublishedSystemPermission
        );
    }

    public Mono<Void> disallowAnonymousAndSystemUsersToReadPublishedOffer(String offerId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(offerId));
        var systemHolder = Holder.group(HolderId.system());
        var anonymousHolder = Holder.group(HolderId.anonymous());

        var readPublishedPermission = Permission.builder()
                .holder(anonymousHolder)
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);
        var readPublishedSystemPermission = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);

        return permissionsService.removePermissions(
                readPublishedPermission,
                readPublishedSystemPermission
        );
    }

    public Mono<Void> updateOfferInLookup(String offerId) {
        return offerService.getOrThrow(OfferId.of(offerId))
                .flatMap(offer -> resolveProductData(offer.getProductId())
                        .map(productData -> LookupOffer.of(
                                offer.getId(),
                                offer.getVersion(),
                                OfferAlias.fromTitle(offer.getTitle()),
                                offer.getTitle(),
                                offer.getSize(),
                                offer.getCategories(),
                                Product.of(offer.getProductId(), productData.getNumber()),
                                offer.getImages(),
                                productData.getLinks(),
                                productData.getFabricComposition(),
                                offer.getPricing(),
                                offer.getNotes(),
                                offer.isPublished(),
                                offer.isReserved(),
                                offer.getCreatedAt(),
                                offer.getArchivedAt().orElse(null)
                        ))
                )
                .flatMap(offerLookupRepo::update);
    }

    public Mono<Void> removeOfferFromLookup(String offerId) {
        return offerLookupRepo.remove(OfferId.of(offerId));
    }

    public Mono<Void> updateProductInLookup(
            String productId,
            long version,
            ProductNumber number,
            List<ImageId> images,
            Links links,
            FabricComposition fabricComposition
    ) {
        var product = LookupProduct.of(
                ProductId.of(productId),
                Version.of(version),
                number,
                images,
                links,
                fabricComposition
        );
        return productForOfferLookupRepo.update(product);
    }

    public Mono<Void> updateProductImagesInLookup(String productId, long version, List<ImageId> images) {
        return productForOfferLookupRepo.findById(ProductId.of(productId))
                .flatMap(product -> productForOfferLookupRepo.update(LookupProduct.of(
                        product.getId(),
                        Version.of(version),
                        product.getNumber(),
                        images,
                        product.getLinks(),
                        product.getFabricComposition()
                )));
    }

    public Mono<Void> addProductLinkInLookup(String productId, long version, Link link) {
        return productForOfferLookupRepo.findById(ProductId.of(productId))
                .flatMap(product -> {
                    var links = new HashSet<>(product.getLinks().getLinks());
                    links.add(link);

                    return productForOfferLookupRepo.update(LookupProduct.of(
                            product.getId(),
                            Version.of(version),
                            product.getNumber(),
                            product.getImages(),
                            Links.of(links),
                            product.getFabricComposition()
                    )).then(updateOfferLookupsForProduct(productId));
                });
    }

    public Mono<Void> removeProductLinkFromLookup(String productId, long version, LinkType linkType, String linkId) {
        return productForOfferLookupRepo.findById(ProductId.of(productId))
                .flatMap(product -> {
                    var links = product.getLinks().getLinks().stream()
                            .filter(l -> !(l.getType() == linkType && l.getId().getValue().equals(linkId)))
                            .collect(Collectors.toSet());

                    return productForOfferLookupRepo.update(LookupProduct.of(
                            product.getId(),
                            Version.of(version),
                            product.getNumber(),
                            product.getImages(),
                            Links.of(links),
                            product.getFabricComposition()
                    )).then(updateOfferLookupsForProduct(productId));
                });
    }

    public Mono<Void> updateProductLinkInLookup(String productId, long version, Link updatedLink) {
        return productForOfferLookupRepo.findById(ProductId.of(productId))
                .flatMap(product -> {
                    var links = product.getLinks().getLinks().stream()
                            .map(l -> (l.getType() == updatedLink.getType() && l.getId().equals(updatedLink.getId()))
                                    ? updatedLink
                                    : l)
                            .collect(Collectors.toSet());

                    return productForOfferLookupRepo.update(LookupProduct.of(
                            product.getId(),
                            Version.of(version),
                            product.getNumber(),
                            product.getImages(),
                            Links.of(links),
                            product.getFabricComposition()
                    ));
                })
                .then(updateOfferLookupsForProduct(productId));
    }

    public Mono<Void> updateProductFabricCompositionInLookup(String productId, long version, FabricComposition fabricComposition) {
        return productForOfferLookupRepo.findById(ProductId.of(productId))
                .flatMap(product -> productForOfferLookupRepo.update(LookupProduct.of(
                        product.getId(),
                        Version.of(version),
                        product.getNumber(),
                        product.getImages(),
                        product.getLinks(),
                        fabricComposition
                )).then(updateOfferLookupsForProduct(productId)));
    }

    public Mono<Void> updateProductNumberInLookup(String productId, long version, ProductNumber number) {
        return productForOfferLookupRepo.findById(ProductId.of(productId))
                .flatMap(product -> productForOfferLookupRepo.update(LookupProduct.of(
                        product.getId(),
                        Version.of(version),
                        number,
                        product.getImages(),
                        product.getLinks(),
                        product.getFabricComposition()
                )).then(updateOfferLookupsForProduct(productId)));
    }

    public Mono<Void> removeProductFromLookup(String productId) {
        return productForOfferLookupRepo.remove(ProductId.of(productId));
    }

    private Mono<Void> updateOfferLookupsForProduct(String productId) {
        return offerLookupRepo.findByProductId(ProductId.of(productId))
                .flatMap(offer -> updateOfferInLookup(offer.getId().getValue()))
                .then();
    }

    private Mono<LookupProduct> resolveProductData(ProductId productId) {
        return productForOfferLookupRepo.findById(productId);
    }

    private OfferDetails toOfferDetails(Offer offer, LookupProduct productData) {
        var product = Product.of(offer.getProductId(), productData.getNumber());

        return OfferDetails.of(
                offer.getId(),
                offer.getVersion(),
                offer.getTitle(),
                offer.getSize(),
                offer.getCategories(),
                product,
                offer.getImages(),
                productData.getLinks(),
                productData.getFabricComposition(),
                offer.getPricing(),
                offer.getNotes(),
                offer.isPublished(),
                offer.isReserved(),
                offer.getCreatedAt(),
                offer.getArchivedAt().orElse(null)
        );
    }

    private OfferDetails toLookupOfferDetails(LookupOffer offer) {
        return OfferDetails.of(
                offer.getId(),
                offer.getVersion(),
                offer.getTitle(),
                offer.getSize(),
                offer.getCategories(),
                offer.getProduct(),
                offer.getImages(),
                offer.getLinks(),
                offer.getFabricComposition(),
                offer.getPricing(),
                offer.getNotes(),
                offer.isPublished(),
                offer.isReserved(),
                offer.getCreatedAt(),
                offer.getArchivedAt().orElse(null)
        );
    }

    private PublishedOffer toPublishedOffer(LookupOffer offer) {
        return PublishedOffer.of(
                offer.getId(),
                offer.getAlias(),
                offer.getTitle(),
                offer.getSize(),
                offer.getCategories(),
                offer.getImages(),
                offer.getLinks(),
                offer.getFabricComposition(),
                offer.getPricing(),
                offer.getNotes()
        );
    }

    private Flux<OfferId> getAccessibleOfferIds(Agent agent) {
        Holder holder = toHolder(agent);
        ResourceType resourceType = getResourceType();

        return permissionsService.findPermissionsByHolderAndResourceType(holder, resourceType)
                .mapNotNull(permission -> permission.getResource()
                        .getId()
                        .map(id -> OfferId.of(id.getValue()))
                        .orElse(null));
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable OfferId offerId) {
        Permission permission = toPermission(agent, action, offerId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable OfferId offerId) {
        Holder holder = toHolder(agent);
        var resourceType = getResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(offerId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(offerId.getValue()))))
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

    private Notes toNotes(NotesDTO dto) {
        return Notes.of(
                Note.of(dto.description),
                dto.contains != null ? Note.of(dto.contains) : null,
                dto.care != null ? Note.of(dto.care) : null,
                dto.safety != null ? Note.of(dto.safety) : null
        );
    }

    private OfferSortProperty toSortProperty(OffersSortPropertyDTO dto) {
        return switch (dto) {
            case ALPHABETICAL -> OfferSortProperty.ALPHABETICAL;
            case NEWEST -> OfferSortProperty.NEWEST;
            case PRICE -> OfferSortProperty.PRICE;
        };
    }

    private OfferSortDirection toSortDirection(OffersSortDirectionDTO dto) {
        return switch (dto) {
            case ASCENDING -> OfferSortDirection.ASCENDING;
            case DESCENDING -> OfferSortDirection.DESCENDING;
        };
    }

    private Mono<Void> assertAliasIsNotAlreadyInUse(OfferAlias alias, @Nullable OfferId excludeId) {
        return offerLookupRepo.findByAlias(alias)
                .filter(offer -> !offer.getId().equals(excludeId))
                .map(LookupOffer::getId)
                .flatMap(conflictingId -> Mono.error(new AliasAlreadyInUseError(conflictingId, alias)));
    }

    private ResourceType getResourceType() {
        return ResourceType.of("OFFER");
    }

}
