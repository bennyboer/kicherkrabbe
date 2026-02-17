package de.bennyboer.kicherkrabbe.products.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.products.ProductsModule;
import de.bennyboer.kicherkrabbe.products.api.LinkDTO;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.RemoveLinkFromLookupRequest;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateLinkInLookupRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class ProductsMessaging {

    record NamedEvent(String name) {
    }

    @Bean("products_onUserCreatedAddPermissionToCreateProductsAndReadLinksMsgListener")
    public EventListener onUserCreatedAddPermissionToCreateProductsAndReadLinksMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.user-created-add-permission-to-create-products-and-read-links",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateProductsAndReadLinks(userId);
                }
        );
    }

    @Bean("products_onUserDeletedRemovePermissionsForUserMsgListener")
    public EventListener onUserDeletedRemovePermissionsForUserMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.user-deleted-remove-permissions-for-user",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean("products_onProductCreatedUpdateProductLookupMsgListener")
    public EventListener onProductCreatedUpdateProductLookupMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.product-created-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("CREATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.updateProductInLookup(productId);
                }
        );
    }

    @Bean("products_onProductLinkAddedUpdateProductLookupMsgListener")
    public EventListener onProductLinkAddedUpdateProductLookupMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.product-link-added-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("LINK_ADDED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.updateProductInLookup(productId);
                }
        );
    }

    @Bean("products_onProductLinkRemovedUpdateProductLookupMsgListener")
    public EventListener onProductLinkRemovedUpdateProductLookupMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.product-link-removed-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("LINK_REMOVED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.updateProductInLookup(productId);
                }
        );
    }

    @Bean("products_onProductLinkUpdatedUpdateProductLookupMsgListener")
    public EventListener onProductLinkUpdatedUpdateProductLookupMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.product-link-updated-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("LINK_UPDATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.updateProductInLookup(productId);
                }
        );
    }

    @Bean("products_onProductImagesUpdatedUpdateProductLookupMsgListener")
    public EventListener onProductImagesUpdatedUpdateProductLookupMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.product-images-updated-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("IMAGES_UPDATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.updateProductInLookup(productId);
                }
        );
    }

    @Bean("products_onProductFabricCompositionUpdatedUpdateProductLookupMsgListener")
    public EventListener onProductFabricCompositionUpdatedUpdateProductLookupMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.product-fabric-composition-updated-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("FABRIC_COMPOSITION_UPDATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.updateProductInLookup(productId);
                }
        );
    }

    @Bean("products_onProductNotesUpdatedUpdateProductLookupMsgListener")
    public EventListener onProductNotesUpdatedUpdateProductLookupMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.product-notes-updated-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("NOTES_UPDATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.updateProductInLookup(productId);
                }
        );
    }

    @Bean("products_onProductProducedAtUpdatedUpdateProductLookupMsgListener")
    public EventListener onProductProducedAtUpdatedUpdateProductLookupMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.product-produced-at-updated-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("PRODUCED_AT_UPDATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.updateProductInLookup(productId);
                }
        );
    }

    @Bean("products_onProductDeletedRemoveProductFromLookupMsgListener")
    public EventListener onProductDeletedRemoveProductFromLookupMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.product-deleted-remove-product-from-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("DELETED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.removeProductFromLookup(productId);
                }
        );
    }

    @Bean("products_onProductDeletedRemovePermissionsForProductMsgListener")
    public EventListener onProductDeletedRemovePermissionsForProductMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.product-deleted-remove-permissions-for-product",
                AggregateType.of("PRODUCT"),
                EventName.of("DELETED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsOnProduct(productId);
                }
        );
    }

    @Bean("products_onProductCreatedAllowUserToReadAndManageProductMsgListener")
    public EventListener onProductCreatedAllowUserToReadAndManageProductMsgListener(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.product-created-allow-user-to-read-and-manage-product",
                AggregateType.of("PRODUCT"),
                EventName.of("CREATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();
                    if (!event.getMetadata().getAgent().isUser()) {
                        return Mono.empty();
                    }
                    String userId = event.getMetadata().getAgent().getId().getValue();

                    return module.allowUserToReadAndManageProduct(productId, userId);
                }
        );
    }

    @Bean("products_onPatternCreatedUpdateLinkInLookup")
    public EventListener onPatternCreatedUpdateLinkInLookup(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.pattern-created-update-link-in-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("CREATED"),
                NamedEvent.class,
                (metadata, event) -> {
                    String patternId = metadata.getAggregateId().getValue();
                    long version = metadata.getAggregateVersion().getValue();

                    var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
                    updateLinkInLookupRequest.link = new LinkDTO();
                    updateLinkInLookupRequest.link.type = LinkTypeDTO.PATTERN;
                    updateLinkInLookupRequest.link.id = patternId;
                    updateLinkInLookupRequest.link.name = event.name();
                    updateLinkInLookupRequest.version = version;
                    return module.updateLinkInLookup(updateLinkInLookupRequest, Agent.system()).then();
                }
        );
    }

    @Bean("products_onPatternRenamedUpdateLinkInLookup")
    public EventListener onPatternRenamedUpdateLinkInLookup(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.pattern-renamed-update-link-in-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("RENAMED"),
                NamedEvent.class,
                (metadata, event) -> {
                    String patternId = metadata.getAggregateId().getValue();
                    long version = metadata.getAggregateVersion().getValue();

                    var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
                    updateLinkInLookupRequest.link = new LinkDTO();
                    updateLinkInLookupRequest.link.type = LinkTypeDTO.PATTERN;
                    updateLinkInLookupRequest.link.id = patternId;
                    updateLinkInLookupRequest.link.name = event.name();
                    updateLinkInLookupRequest.version = version;
                    return module.updateLinkInLookup(updateLinkInLookupRequest, Agent.system()).then();
                }
        );
    }

    @Bean("products_onPatternDeletedRemoveLinkFromLookup")
    public EventListener onPatternDeletedRemoveLinkFromLookup(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.pattern-deleted-remove-link-from-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("DELETED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    var request = new RemoveLinkFromLookupRequest();
                    request.linkType = LinkTypeDTO.PATTERN;
                    request.linkId = patternId;
                    return module.removeLinkFromLookup(request, Agent.system()).then();
                }
        );
    }

    @Bean("products_onFabricCreatedUpdateLinkInLookup")
    public EventListener onFabricCreatedUpdateLinkInLookup(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.fabric-created-update-link-in-lookup",
                AggregateType.of("FABRIC"),
                EventName.of("CREATED"),
                NamedEvent.class,
                (metadata, event) -> {
                    String fabricId = metadata.getAggregateId().getValue();
                    long version = metadata.getAggregateVersion().getValue();

                    var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
                    updateLinkInLookupRequest.link = new LinkDTO();
                    updateLinkInLookupRequest.link.type = LinkTypeDTO.FABRIC;
                    updateLinkInLookupRequest.link.id = fabricId;
                    updateLinkInLookupRequest.link.name = event.name();
                    updateLinkInLookupRequest.version = version;
                    return module.updateLinkInLookup(updateLinkInLookupRequest, Agent.system()).then();
                }
        );
    }

    @Bean("products_onFabricRenamedUpdateLinkInLookup")
    public EventListener onFabricRenamedUpdateLinkInLookup(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.fabric-renamed-update-link-in-lookup",
                AggregateType.of("FABRIC"),
                EventName.of("RENAMED"),
                NamedEvent.class,
                (metadata, event) -> {
                    String fabricId = metadata.getAggregateId().getValue();
                    long version = metadata.getAggregateVersion().getValue();

                    var updateLinkInLookupRequest = new UpdateLinkInLookupRequest();
                    updateLinkInLookupRequest.link = new LinkDTO();
                    updateLinkInLookupRequest.link.type = LinkTypeDTO.FABRIC;
                    updateLinkInLookupRequest.link.id = fabricId;
                    updateLinkInLookupRequest.link.name = event.name();
                    updateLinkInLookupRequest.version = version;
                    return module.updateLinkInLookup(updateLinkInLookupRequest, Agent.system()).then();
                }
        );
    }

    @Bean("products_onFabricDeletedRemoveLinkFromLookup")
    public EventListener onFabricDeletedRemoveLinkFromLookup(
            EventListenerFactory factory,
            ProductsModule module
    ) {
        return factory.createEventListenerForEvent(
                "products.fabric-deleted-remove-link-from-lookup",
                AggregateType.of("FABRIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    var request = new RemoveLinkFromLookupRequest();
                    request.linkType = LinkTypeDTO.FABRIC;
                    request.linkId = fabricId;
                    return module.removeLinkFromLookup(request, Agent.system()).then();
                }
        );
    }

}
