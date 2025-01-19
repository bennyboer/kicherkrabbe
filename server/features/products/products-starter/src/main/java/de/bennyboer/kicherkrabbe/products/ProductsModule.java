package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.*;
import de.bennyboer.kicherkrabbe.products.api.responses.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;

@AllArgsConstructor
public class ProductsModule {

    public Mono<QueryProductsResponse> getProducts(
            String searchTerm,
            @Nullable Instant from,
            @Nullable Instant to,
            long skip,
            long limit,
            Agent agent
    ) {
        return Mono.empty(); // TODO
    }

    public Mono<QueryProductResponse> getProduct(String productId, Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<QueryLinksResponse> getLinks(String searchTerm, long skip, long limit, Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<CreateProductResponse> createProduct(CreateProductRequest req, Agent agent) {
        return Mono.empty(); // TODO
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
        return Mono.empty(); // TODO
    }

    public Mono<DeleteProductResponse> deleteProduct(String productId, long version, Agent agent) {
        return Mono.empty(); // TODO
    }

}
