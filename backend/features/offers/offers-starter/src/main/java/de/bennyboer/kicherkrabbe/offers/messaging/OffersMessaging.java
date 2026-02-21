package de.bennyboer.kicherkrabbe.offers.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.offers.*;
import de.bennyboer.kicherkrabbe.offers.OffersModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class OffersMessaging {

    @Bean("offers_onUserCreatedAllowUserToCreateOffers")
    public EventListener onUserCreatedAllowUserToCreateOffers(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.user-created-allow-user-to-create-offers",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateOffers(userId);
                }
        );
    }

    @Bean("offers_onUserDeletedRemoveOffersPermissionsForUser")
    public EventListener onUserDeletedRemoveOffersPermissionsForUser(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean("offers_onOfferCreatedOrUpdatedUpdateLookup")
    public EventListener onOfferCreatedOrUpdatedUpdateLookup(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForAllEvents(
                "offers.offer-created-or-updated-update-lookup",
                AggregateType.of("OFFER"),
                (event) -> {
                    String offerId = event.getMetadata().getAggregateId().getValue();
                    boolean isDeleted = event.getEventName().equals(EventName.of("DELETED"));
                    if (isDeleted) {
                        return Mono.empty();
                    }

                    return module.updateOfferInLookup(offerId);
                }
        );
    }

    @Bean("offers_onOfferDeletedRemoveFromLookup")
    public EventListener onOfferDeletedRemoveFromLookup(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.offer-deleted-remove-from-lookup",
                AggregateType.of("OFFER"),
                EventName.of("DELETED"),
                (event) -> {
                    String offerId = event.getMetadata().getAggregateId().getValue();

                    return module.removeOfferFromLookup(offerId);
                }
        );
    }

    @Bean("offers_onOfferCreatedAllowUserToManageOffer")
    public EventListener onOfferCreatedAllowUserToManageOffer(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.offer-created-allow-user-to-manage-offer",
                AggregateType.of("OFFER"),
                EventName.of("CREATED"),
                (event) -> {
                    String offerId = event.getMetadata().getAggregateId().getValue();
                    String userId = event.getMetadata().getAgent().getId().getValue();

                    return module.allowUserToManageOffer(offerId, userId);
                }
        );
    }

    @Bean("offers_onOfferDeletedRemovePermissionsOnOffer")
    public EventListener onOfferDeletedRemovePermissionsOnOffer(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.offer-deleted-remove-permissions",
                AggregateType.of("OFFER"),
                EventName.of("DELETED"),
                (event) -> {
                    String offerId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsOnOffer(offerId);
                }
        );
    }

    @Bean("offers_onOfferPublishedAllowAnonymousAndSystemUsersToReadPublishedOffer")
    public EventListener onOfferPublishedAllowAnonymousAndSystemUsersToReadPublishedOffer(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.offer-published-allow-anonymous-and-system-users-to-read-published-offer",
                AggregateType.of("OFFER"),
                EventName.of("PUBLISHED"),
                (event) -> {
                    String offerId = event.getMetadata().getAggregateId().getValue();

                    return module.allowAnonymousAndSystemUsersToReadPublishedOffer(offerId);
                }
        );
    }

    @Bean("offers_onOfferUnpublishedDisallowAnonymousAndSystemUsersToReadPublishedOffer")
    public EventListener onOfferUnpublishedDisallowAnonymousAndSystemUsersToReadPublishedOffer(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.offer-unpublished-disallow-anonymous-and-system-users-to-read-published-offer",
                AggregateType.of("OFFER"),
                EventName.of("UNPUBLISHED"),
                (event) -> {
                    String offerId = event.getMetadata().getAggregateId().getValue();

                    return module.disallowAnonymousAndSystemUsersToReadPublishedOffer(offerId);
                }
        );
    }

    @Bean("offers_onOfferArchivedDisallowAnonymousAndSystemUsersToReadPublishedOffer")
    public EventListener onOfferArchivedDisallowAnonymousAndSystemUsersToReadPublishedOffer(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.offer-archived-disallow-anonymous-and-system-users-to-read-published-offer",
                AggregateType.of("OFFER"),
                EventName.of("ARCHIVED"),
                (event) -> {
                    String offerId = event.getMetadata().getAggregateId().getValue();

                    return module.disallowAnonymousAndSystemUsersToReadPublishedOffer(offerId);
                }
        );
    }

    @Bean("offers_onProductCreatedUpdateProductLookup")
    public EventListener onProductCreatedUpdateProductLookup(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.product-created-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("CREATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();
                    long version = event.getMetadata().getAggregateVersion().getValue();
                    var payload = event.getEvent();

                    var number = ProductNumber.of((String) payload.get("number"));
                    var images = deserializeImageIds(payload.get("images"));
                    var links = deserializeLinks(payload.get("links"));
                    var fabricComposition = deserializeFabricComposition(payload.get("fabricComposition"));

                    return module.updateProductInLookup(productId, version, number, images, links, fabricComposition);
                }
        );
    }

    @Bean("offers_onProductImagesUpdatedUpdateProductLookup")
    public EventListener onProductImagesUpdatedUpdateProductLookup(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.product-images-updated-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("IMAGES_UPDATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();
                    long version = event.getMetadata().getAggregateVersion().getValue();
                    var payload = event.getEvent();

                    var images = deserializeImageIds(payload.get("images"));

                    return module.updateProductImagesInLookup(productId, version, images);
                }
        );
    }

    @Bean("offers_onProductDeletedRemoveProductLookup")
    public EventListener onProductDeletedRemoveProductLookup(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.product-deleted-remove-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("DELETED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.removeProductFromLookup(productId);
                }
        );
    }

    @Bean("offers_onProductLinkAddedUpdateProductLookup")
    public EventListener onProductLinkAddedUpdateProductLookup(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.product-link-added-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("LINK_ADDED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();
                    long version = event.getMetadata().getAggregateVersion().getValue();
                    var payload = event.getEvent();

                    var link = deserializeLink(payload);

                    return module.addProductLinkInLookup(productId, version, link);
                }
        );
    }

    @Bean("offers_onProductLinkRemovedUpdateProductLookup")
    public EventListener onProductLinkRemovedUpdateProductLookup(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.product-link-removed-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("LINK_REMOVED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();
                    long version = event.getMetadata().getAggregateVersion().getValue();
                    var payload = event.getEvent();

                    var linkType = LinkType.valueOf((String) payload.get("type"));
                    var linkId = (String) payload.get("id");

                    return module.removeProductLinkFromLookup(productId, version, linkType, linkId);
                }
        );
    }

    @Bean("offers_onProductLinkUpdatedUpdateProductLookup")
    public EventListener onProductLinkUpdatedUpdateProductLookup(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.product-link-updated-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("LINK_UPDATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();
                    long version = event.getMetadata().getAggregateVersion().getValue();
                    var payload = event.getEvent();

                    var link = deserializeLink(payload);

                    return module.updateProductLinkInLookup(productId, version, link);
                }
        );
    }

    @Bean("offers_onProductFabricCompositionUpdatedUpdateProductLookup")
    public EventListener onProductFabricCompositionUpdatedUpdateProductLookup(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.product-fabric-composition-updated-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("FABRIC_COMPOSITION_UPDATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();
                    long version = event.getMetadata().getAggregateVersion().getValue();
                    var payload = event.getEvent();

                    var fabricComposition = deserializeFabricComposition(payload.get("fabricComposition"));

                    return module.updateProductFabricCompositionInLookup(productId, version, fabricComposition);
                }
        );
    }

    @Bean("offers_onProductNumberUpdatedUpdateProductLookup")
    public EventListener onProductNumberUpdatedUpdateProductLookup(
            EventListenerFactory factory,
            OffersModule module
    ) {
        return factory.createEventListenerForEvent(
                "offers.product-number-updated-update-product-lookup",
                AggregateType.of("PRODUCT"),
                EventName.of("PRODUCT_NUMBER_UPDATED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();
                    long version = event.getMetadata().getAggregateVersion().getValue();
                    var payload = event.getEvent();

                    var number = ProductNumber.of((String) payload.get("number"));

                    return module.updateProductNumberInLookup(productId, version, number);
                }
        );
    }

    @SuppressWarnings("unchecked")
    private List<ImageId> deserializeImageIds(Object imagesPayload) {
        var imagesList = (List<String>) imagesPayload;
        return imagesList.stream()
                .map(ImageId::of)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private Links deserializeLinks(Object linksPayload) {
        var linksList = (List<Map<String, Object>>) linksPayload;
        var links = linksList.stream()
                .map(this::deserializeLink)
                .collect(Collectors.toSet());
        return Links.of(links);
    }

    private Link deserializeLink(Map<String, Object> payload) {
        return Link.of(
                LinkType.valueOf((String) payload.get("type")),
                LinkId.of((String) payload.get("id")),
                LinkName.of((String) payload.get("name"))
        );
    }

    @SuppressWarnings("unchecked")
    private FabricComposition deserializeFabricComposition(Object compositionPayload) {
        var itemsList = (List<Map<String, Object>>) compositionPayload;
        var items = itemsList.stream()
                .map(item -> FabricCompositionItem.of(
                        FabricType.valueOf((String) item.get("fabricType")),
                        LowPrecisionFloat.of(((Number) item.get("percentage")).longValue())
                ))
                .collect(Collectors.toSet());
        return FabricComposition.of(items);
    }

}
