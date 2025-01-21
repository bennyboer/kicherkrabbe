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
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.ProductLookupRepo;
import de.bennyboer.kicherkrabbe.products.product.*;
import de.bennyboer.kicherkrabbe.products.transformer.FabricCompositionTransformer;
import de.bennyboer.kicherkrabbe.products.transformer.LinksTransformer;
import de.bennyboer.kicherkrabbe.products.transformer.LookupProductTransformer;
import de.bennyboer.kicherkrabbe.products.transformer.NotesTransformer;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.products.Actions.*;

@AllArgsConstructor
public class ProductsModule {

    private static final CounterId PRODUCT_NUMBER_COUNTER_ID = CounterId.of("PRODUCT_NUMBER");

    private final ProductService productService;

    private final CounterService counterService;

    private final ProductLookupRepo productLookupRepo;

    private final PermissionsService permissionsService;

    private final ReactiveTransactionManager transactionManager;

    public Mono<QueryProductsResponse> getProducts(
            String searchTerm,
            @Nullable Instant from,
            @Nullable Instant to,
            long skip,
            long limit,
            Agent agent
    ) {
        // TODO Check first which products we are able to read as the current agent (getAccessibleProductIds(agent, READ))

        return Mono.empty(); // TODO
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
        return Mono.empty(); // TODO
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
        return Mono.empty(); // TODO
    }

    public Mono<RemoveLinkFromProductResponse> removeLinkFromProduct(String productId, long version, LinkTypeDTO linkType, String linkId, Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<UpdateNotesResponse> updateNotes(String productId, UpdateNotesRequest req, Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<UpdateFabricCompositionResponse> updateFabricComposition(String productId, UpdateFabricCompositionRequest req, Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<UpdateImagesResponse> updateImages(String productId, UpdateImagesRequest req, Agent agent) {
        return Mono.empty(); // TODO
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

    public Mono<Void> allowUserToCreateProducts(String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resourceType = getProductResourceType();

        var createPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(CREATE)
                .onType(resourceType);

        return permissionsService.addPermission(createPermission);
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

    private Mono<ProductNumber> generateNextProductNumber() {
        return getProductCounter()
                .flatMap(counter -> counterService.increment(counter.getId(), counter.getVersion(), Agent.system())
                        .flatMap(version -> counterService.get(counter.getId(), version)))
                .map(counter -> ProductNumber.of(String.format("%010d", counter.getValue())));
    }

    private Mono<Counter> getProductCounter() {
        return counterService.get(PRODUCT_NUMBER_COUNTER_ID)
                .switchIfEmpty(counterService.init(PRODUCT_NUMBER_COUNTER_ID, Agent.system())
                        .flatMap(idAndVersion -> counterService.get(idAndVersion.getId())));
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

}
