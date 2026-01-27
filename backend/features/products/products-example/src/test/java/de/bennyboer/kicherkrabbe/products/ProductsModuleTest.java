package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import de.bennyboer.kicherkrabbe.products.api.*;
import de.bennyboer.kicherkrabbe.products.api.requests.*;
import de.bennyboer.kicherkrabbe.products.api.responses.*;
import de.bennyboer.kicherkrabbe.products.samples.SampleProduct;
import de.bennyboer.kicherkrabbe.products.counter.CounterService;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.LinkLookupRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.inmemory.InMemoryLinkLookupRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.ProductLookupRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.inmemory.InMemoryProductLookupRepo;
import de.bennyboer.kicherkrabbe.products.product.ProductService;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import jakarta.annotation.Nullable;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

public class ProductsModuleTest {

    private final TestClock clock = new TestClock();

    private final ProductsModuleConfig config = new ProductsModuleConfig();

    private final ProductService productService = new ProductService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            clock
    );

    private final CounterService counterService = new CounterService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            clock
    );

    private final ProductLookupRepo productLookupRepo = new InMemoryProductLookupRepo();

    private final LinkLookupRepo linkLookupRepo = new InMemoryLinkLookupRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    private final ReactiveTransactionManager transactionManager = new MockReactiveTransactionManager();

    private final ProductsModule module = config.productsModule(
            productService,
            counterService,
            productLookupRepo,
            linkLookupRepo,
            permissionsService,
            transactionManager
    );

    public void setTime(Instant instant) {
        clock.setNow(instant);
    }

    public QueryProductsResponse getProducts(
            String searchTerm,
            @Nullable Instant from,
            @Nullable Instant to,
            long skip,
            long limit,
            Agent agent
    ) {
        return module.getProducts(searchTerm, from, to, skip, limit, agent).block();
    }

    public QueryProductResponse getProduct(String productId, Agent agent) {
        return module.getProduct(productId, agent).block();
    }

    public QueryLinksResponse getLinks(String searchTerm, long skip, long limit, Agent agent) {
        return module.getLinks(searchTerm, skip, limit, agent).block();
    }

    public void updateLinkInLookup(UpdateLinkInLookupRequest req, Agent agent) {
        var productIds = module.updateLinkInLookup(req, agent).collectList().block();

        for (var productId : productIds) {
            updateProductInLookup(productId);
        }
    }

    public void removeLinkFromLookup(RemoveLinkFromLookupRequest req, Agent agent) {
        var productIds = module.removeLinkFromLookup(req, agent).collectList().block();

        for (var productId : productIds) {
            updateProductInLookup(productId);
        }
    }

    public CreateProductResponse createProduct(SampleProduct sample, Agent agent) {
        return createProduct(sample.toRequest(), agent);
    }

    public CreateProductResponse createSampleProduct(Agent agent) {
        return createProduct(SampleProduct.builder().build(), agent);
    }

    public CreateProductResponse createProduct(CreateProductRequest req, Agent agent) {
        var result = module.createProduct(req, agent).block();

        updateProductInLookup(result.id);

        if (agent.isUser()) {
            allowUserToReadAndManageProduct(result.id, agent.getId().getValue());
        }

        return result;
    }

    public AddLinkToProductResponse addLinkToProduct(String productId, AddLinkToProductRequest req, Agent agent) {
        var result = module.addLinkToProduct(productId, req, agent).block();

        updateProductInLookup(productId);

        return result;
    }

    public RemoveLinkFromProductResponse removeLinkFromProduct(
            String productId,
            long version,
            LinkTypeDTO linkType,
            String linkId,
            Agent agent) {
        var result = module.removeLinkFromProduct(productId, version, linkType, linkId, agent).block();

        updateProductInLookup(productId);

        return result;
    }

    public UpdateNotesResponse updateNotes(String productId, UpdateNotesRequest req, Agent agent) {
        var result = module.updateNotes(productId, req, agent).block();

        updateProductInLookup(productId);

        return result;
    }

    public UpdateFabricCompositionResponse updateFabricComposition(
            String productId,
            UpdateFabricCompositionRequest req,
            Agent agent
    ) {
        var result = module.updateFabricComposition(productId, req, agent).block();

        updateProductInLookup(productId);

        return result;
    }

    public UpdateImagesResponse updateImages(String productId, UpdateImagesRequest req, Agent agent) {
        var result = module.updateImages(productId, req, agent).block();

        updateProductInLookup(productId);

        return result;
    }

    public UpdateProducedAtDateResponse updateProducedAt(
            String productId,
            UpdateProducedAtDateRequest req,
            Agent agent
    ) {
        var result = module.updateProducedAt(productId, req, agent).block();

        updateProductInLookup(productId);

        return result;
    }

    public DeleteProductResponse deleteProduct(String productId, long version, Agent agent) {
        var result = module.deleteProduct(productId, version, agent).block();

        removeProductFromLookup(productId);
        if (agent.isUser()) {
            removePermissionsOnProduct(productId);
        }

        return result;
    }

    public void allowUserToCreateProductsAndReadLinks(String userId) {
        module.allowUserToCreateProductsAndReadLinks(userId).block();
    }

    public void allowSystemUserToUpdateAndDeleteLinks() {
        module.allowSystemUserToUpdateAndDeleteLinksAndManageProductLinks().block();
    }

    private void allowUserToReadAndManageProduct(String productId, String userId) {
        module.allowUserToReadAndManageProduct(productId, userId).block();
    }

    private void removePermissionsOnProduct(String productId) {
        module.removePermissionsOnProduct(productId).block();
    }

    private void updateProductInLookup(String productId) {
        module.updateProductInLookup(productId).block();
    }

    private void removeProductFromLookup(String productId) {
        module.removeProductFromLookup(productId).block();
    }

}
