package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.*;
import de.bennyboer.kicherkrabbe.products.api.responses.*;
import de.bennyboer.kicherkrabbe.products.counter.Counter;
import de.bennyboer.kicherkrabbe.products.counter.CounterId;
import de.bennyboer.kicherkrabbe.products.counter.CounterService;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.LinkLookupRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.LookupLink;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.ProductLookupRepo;
import de.bennyboer.kicherkrabbe.products.product.*;
import de.bennyboer.kicherkrabbe.products.transformer.*;
import jakarta.annotation.Nullable;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.products.Actions.*;

public class ProductsModule {

    private static final CounterId PRODUCT_NUMBER_COUNTER_ID = CounterId.of("PRODUCT_NUMBER");

    private final ProductService productService;

    private final CounterService counterService;

    private final ProductLookupRepo productLookupRepo;

    private final LinkLookupRepo linkLookupRepo;

    private final PermissionsService permissionsService;

    private final ReactiveTransactionManager transactionManager;

    public ProductsModule(
            ProductService productService,
            CounterService counterService,
            ProductLookupRepo productLookupRepo,
            LinkLookupRepo linkLookupRepo,
            PermissionsService permissionsService,
            ReactiveTransactionManager transactionManager
    ) {
        this.productService = productService;
        this.counterService = counterService;
        this.productLookupRepo = productLookupRepo;
        this.linkLookupRepo = linkLookupRepo;
        this.permissionsService = permissionsService;
        this.transactionManager = transactionManager;
    }

    private boolean isInitialized = false;

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent ignoredEvent) {
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        initialize()
                .as(transactionalOperator::transactional)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public Mono<Void> initialize() {
        return allowSystemUserToUpdateAndDeleteLinksAndManageProductLinks();
    }

    public Mono<QueryProductsResponse> getProducts(
            String searchTerm,
            @Nullable Instant from,
            @Nullable Instant to,
            long skip,
            long limit,
            Agent agent
    ) {
        return getAccessibleProductIds(READ, agent)
                .collect(Collectors.toSet())
                .flatMap(ids -> productLookupRepo.findByIds(ids, searchTerm, from, to, skip, limit))
                .map(page -> {
                    var result = new QueryProductsResponse();
                    result.total = page.getTotal();
                    result.products = LookupProductTransformer.toApi(page.getProducts());
                    return result;
                });
    }

    public Mono<QueryProductResponse> getProduct(String productId, Agent agent) {
        var id = ProductId.of(productId);

        return assertAgentIsAllowedToOnProduct(agent, READ, id)
                .then(productLookupRepo.findById(id))
                .map(product -> {
                    var result = new QueryProductResponse();
                    result.product = LookupProductTransformer.toApi(product);
                    return result;
                });
    }

    public Mono<QueryLinksResponse> getLinks(String searchTerm, long skip, long limit, Agent agent) {
        return assertAgentIsAllowedToOnLinks(agent, READ)
                .then(linkLookupRepo.find(searchTerm, skip, limit))
                .map(page -> {
                    var result = new QueryLinksResponse();
                    result.total = page.getTotal();
                    result.links = LinkTransformer.toApi(page.getLinks());
                    return result;
                });
    }

    public Flux<String> updateLinkInLookup(UpdateLinkInLookupRequest req, Agent agent) {
        Link link = LinkTransformer.toInternal(req.link);

        return assertAgentIsAllowedToOnLinks(agent, UPDATE)
                .then(linkLookupRepo.update(LookupLink.create(link)))
                .thenMany(updateLinkInProducts(link.getType(), link.getId(), agent))
                .map(ProductId::getValue);
    }

    public Flux<String> removeLinkFromLookup(RemoveLinkFromLookupRequest req, Agent agent) {
        LinkType linkType = LinkTypeTransformer.toInternal(req.linkType);
        LinkId linkId = LinkId.of(req.linkId);

        return assertAgentIsAllowedToOnLinks(agent, DELETE)
                .then(linkLookupRepo.remove(linkType, linkId))
                .thenMany(removeLinkFromProducts(linkType, linkId, agent))
                .map(ProductId::getValue);
    }

    public Mono<CreateProductResponse> createProduct(CreateProductRequest req, Agent agent) {
        notNull(req.images, "Images must be given");
        notNull(req.producedAt, "Produced at date must be given");

        List<ImageId> images = req.images
                .stream()
                .map(ImageId::of)
                .toList();
        Links links = LinksTransformer.toInternal(req.links);
        FabricComposition fabricComposition = FabricCompositionTransformer.toInternal(req.fabricComposition);
        Notes notes = NotesTransformer.toInternal(req.notes);
        Instant producedAt = req.producedAt;

        return assertAgentIsAllowedToOnProducts(agent, CREATE)
                .then(generateNextProductNumber())
                .flatMap(number -> productService.create(
                        number,
                        images,
                        links,
                        fabricComposition,
                        notes,
                        producedAt,
                        agent
                ))
                .map(idAndVersion -> {
                    var result = new CreateProductResponse();
                    result.id = idAndVersion.getId().getValue();
                    result.version = idAndVersion.getVersion().getValue();
                    return result;
                });
    }

    public Mono<AddLinkToProductResponse> addLinkToProduct(String productId, AddLinkToProductRequest req, Agent agent) {
        var internalProductId = ProductId.of(productId);
        var internalVersion = Version.of(req.version);

        LinkType linkType = LinkTypeTransformer.toInternal(req.linkType);
        LinkId linkId = LinkId.of(req.linkId);

        return assertAgentIsAllowedToOnProduct(agent, ADD_LINKS, internalProductId)
                .then(linkLookupRepo.findOne(linkType, linkId))
                .flatMap(link -> productService.addLink(internalProductId, internalVersion, link, agent))
                .map(updatedVersion -> {
                    var result = new AddLinkToProductResponse();
                    result.version = updatedVersion.getValue();
                    return result;
                });
    }

    public Mono<RemoveLinkFromProductResponse> removeLinkFromProduct(String productId, long version, LinkTypeDTO linkType, String linkId, Agent agent) {
        var internalProductId = ProductId.of(productId);
        var internalVersion = Version.of(version);
        var internalLinkType = LinkTypeTransformer.toInternal(linkType);
        var internalLinkId = LinkId.of(linkId);

        return assertAgentIsAllowedToOnProduct(agent, REMOVE_LINKS, internalProductId)
                .then(productService.removeLink(
                        internalProductId,
                        internalVersion,
                        internalLinkType,
                        internalLinkId,
                        agent
                ))
                .map(updatedVersion -> {
                    var result = new RemoveLinkFromProductResponse();
                    result.version = updatedVersion.getValue();
                    return result;
                });
    }

    public Mono<UpdateNotesResponse> updateNotes(String productId, UpdateNotesRequest req, Agent agent) {
        var internalProductId = ProductId.of(productId);
        var internalVersion = Version.of(req.version);

        Notes notes = NotesTransformer.toInternal(req.notes);

        return assertAgentIsAllowedToOnProduct(agent, UPDATE_NOTES, internalProductId)
                .then(productService.updateNotes(internalProductId, internalVersion, notes, agent))
                .map(updatedVersion -> {
                    var result = new UpdateNotesResponse();
                    result.version = updatedVersion.getValue();
                    return result;
                });
    }

    public Mono<UpdateFabricCompositionResponse> updateFabricComposition(String productId, UpdateFabricCompositionRequest req, Agent agent) {
        var internalProductId = ProductId.of(productId);
        var internalVersion = Version.of(req.version);

        FabricComposition fabricComposition = FabricCompositionTransformer.toInternal(req.fabricComposition);

        return assertAgentIsAllowedToOnProduct(agent, UPDATE_FABRIC_COMPOSITION, internalProductId)
                .then(productService.updateFabricComposition(internalProductId, internalVersion, fabricComposition, agent))
                .map(updatedVersion -> {
                    var result = new UpdateFabricCompositionResponse();
                    result.version = updatedVersion.getValue();
                    return result;
                });
    }

    public Mono<UpdateImagesResponse> updateImages(String productId, UpdateImagesRequest req, Agent agent) {
        notNull(req.images, "Images must be given");

        var internalProductId = ProductId.of(productId);
        var internalVersion = Version.of(req.version);

        List<ImageId> images = req.images
                .stream()
                .map(ImageId::of)
                .toList();

        return assertAgentIsAllowedToOnProduct(agent, UPDATE_IMAGES, internalProductId)
                .then(productService.updateImages(internalProductId, internalVersion, images, agent))
                .map(updatedVersion -> {
                    var result = new UpdateImagesResponse();
                    result.version = updatedVersion.getValue();
                    return result;
                });
    }

    public Mono<UpdateProducedAtDateResponse> updateProducedAt(String productId, UpdateProducedAtDateRequest req, Agent agent) {
        var internalProductId = ProductId.of(productId);
        var internalVersion = Version.of(req.version);

        return assertAgentIsAllowedToOnProduct(agent, UPDATE_PRODUCED_AT_DATE, internalProductId)
                .then(productService.updateProducedAt(internalProductId, internalVersion, req.producedAt, agent))
                .map(updatedVersion -> {
                    var result = new UpdateProducedAtDateResponse();
                    result.version = updatedVersion.getValue();
                    return result;
                });
    }

    public Mono<DeleteProductResponse> deleteProduct(String productId, long version, Agent agent) {
        var internalProductId = ProductId.of(productId);
        var internalVersion = Version.of(version);

        return assertAgentIsAllowedToOnProduct(agent, DELETE, internalProductId)
                .then(productService.delete(internalProductId, internalVersion, agent))
                .map(updatedVersion -> {
                    var result = new DeleteProductResponse();
                    result.version = updatedVersion.getValue();
                    return result;
                });
    }

    public Mono<Void> updateProductInLookup(String productId) {
        return productService.getOrThrow(ProductId.of(productId))
                .map(product -> LookupProduct.of(
                        product.getId(),
                        product.getVersion(),
                        product.getNumber(),
                        product.getImages(),
                        product.getLinks(),
                        product.getFabricComposition(),
                        product.getNotes(),
                        product.getProducedAt(),
                        product.getCreatedAt()
                ))
                .flatMap(productLookupRepo::update);
    }

    public Mono<Void> removeProductFromLookup(String productId) {
        return productLookupRepo.remove(ProductId.of(productId));
    }

    public Mono<Void> allowUserToCreateProductsAndReadLinks(String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var productResourceType = getProductResourceType();
        var linkResourceType = getLinkResourceType();

        var createPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(CREATE)
                .onType(productResourceType);
        var readLinksPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .onType(linkResourceType);

        return permissionsService.addPermissions(
                createPermission,
                readLinksPermission
        );
    }

    public Mono<Void> allowUserToReadAndManageProduct(String productId, String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resourceType = getProductResourceType();
        var resource = Resource.of(resourceType, ResourceId.of(productId));

        var readPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .on(resource);
        var addLinksPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(ADD_LINKS)
                .on(resource);
        var removeLinksPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(REMOVE_LINKS)
                .on(resource);
        var updateProducedAtDatePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_PRODUCED_AT_DATE)
                .on(resource);
        var updateNotesPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_NOTES)
                .on(resource);
        var updateFabricCompositionPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_FABRIC_COMPOSITION)
                .on(resource);
        var updateImagesPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_IMAGES)
                .on(resource);
        var deletePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermissions(
                readPermission,
                addLinksPermission,
                removeLinksPermission,
                updateProducedAtDatePermission,
                updateNotesPermission,
                updateFabricCompositionPermission,
                updateImagesPermission,
                deletePermission
        );
    }

    public Mono<Void> removePermissionsOnProduct(String productId) {
        var resourceType = getProductResourceType();
        var resource = Resource.of(resourceType, ResourceId.of(productId));

        return permissionsService.removePermissionsByResource(resource);
    }

    public Mono<Void> allowSystemUserToUpdateAndDeleteLinksAndManageProductLinks() {
        var systemHolder = Holder.group(HolderId.system());
        var productResourceType = getProductResourceType();
        var linkResourceType = getLinkResourceType();

        var updateLinks = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(UPDATE)
                .onType(linkResourceType);
        var deleteLinks = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(DELETE)
                .onType(linkResourceType);
        var removeLinksFromProducts = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(REMOVE_LINKS)
                .onType(productResourceType);
        var updateLinksInProducts = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(UPDATE_LINKS)
                .onType(productResourceType);

        return permissionsService.addPermissions(
                updateLinks,
                deleteLinks,
                removeLinksFromProducts,
                updateLinksInProducts
        );
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        return permissionsService.removePermissionsByHolder(Holder.user(HolderId.of(userId)));
    }

    private Flux<ProductId> removeLinkFromProducts(LinkType type, LinkId linkId, Agent agent) {
        return productLookupRepo.findByLink(type, linkId)
                .concatMap(product -> productService.removeLink(
                        product.getId(),
                        product.getVersion(),
                        type,
                        linkId,
                        agent
                ).map(updatedVersion -> product.getId()));
    }

    private Flux<ProductId> updateLinkInProducts(LinkType type, LinkId linkId, Agent agent) {
        return linkLookupRepo.findOne(type, linkId)
                .flatMapMany(link -> productLookupRepo.findByLink(type, linkId)
                        .concatMap(product -> productService.updateLink(
                                product.getId(),
                                product.getVersion(),
                                link,
                                agent
                        ).map(updatedVersion -> product.getId())));
    }

    private Mono<ProductNumber> generateNextProductNumber() {
        return getProductCounter()
                .flatMap(counter -> counterService.increment(counter.getId(), counter.getVersion(), Agent.system())
                        .flatMap(version -> counterService.get(counter.getId(), version)))
                .map(counter -> ProductNumber.of(String.format("%05d", counter.getValue())));
    }

    private Mono<Counter> getProductCounter() {
        return counterService.get(PRODUCT_NUMBER_COUNTER_ID)
                .switchIfEmpty(counterService.init(PRODUCT_NUMBER_COUNTER_ID, Agent.system())
                        .flatMap(idAndVersion -> counterService.get(idAndVersion.getId())));
    }

    private Flux<ProductId> getAccessibleProductIds(Action action, Agent agent) {
        Holder holder = toHolder(agent);
        ResourceType resourceType = getProductResourceType();

        return permissionsService.findPermissionsByHolderAndResourceTypeAndAction(holder, resourceType, action)
                .mapNotNull(permission -> permission.getResource()
                        .getId()
                        .map(id -> ProductId.of(id.getValue()))
                        .orElse(null));
    }

    private Mono<Void> assertAgentIsAllowedToOnLinks(Agent agent, Action action) {
        return assertAgentIsAllowedToOnLink(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedToOnLink(Agent agent, Action action, @Nullable LinkId linkId) {
        Permission permission = toLinkPermission(agent, action, linkId);
        return permissionsService.assertHasPermission(permission);
    }

    private Mono<Void> assertAgentIsAllowedToOnProducts(Agent agent, Action action) {
        return assertAgentIsAllowedToOnProduct(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedToOnProduct(Agent agent, Action action, @Nullable ProductId productId) {
        Permission permission = toProductPermission(agent, action, productId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toProductPermission(Agent agent, Action action, @Nullable ProductId productId) {
        Holder holder = toHolder(agent);
        var resourceType = getProductResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(productId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(productId.getValue()))))
                .orElseGet(() -> permissionBuilder.onType(resourceType));
    }

    private Permission toLinkPermission(Agent agent, Action action, @Nullable LinkId linkId) {
        Holder holder = toHolder(agent);
        var resourceType = getLinkResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(linkId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(linkId.getValue()))))
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

    private ResourceType getProductResourceType() {
        return ResourceType.of("PRODUCT");
    }

    private ResourceType getLinkResourceType() {
        return ResourceType.of("PRODUCT_LINK");
    }

}
