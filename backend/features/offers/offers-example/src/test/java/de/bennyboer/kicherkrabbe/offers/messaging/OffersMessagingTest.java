package de.bennyboer.kicherkrabbe.offers.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.offers.*;
import de.bennyboer.kicherkrabbe.offers.OffersModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Import(OffersMessaging.class)
public class OffersMessagingTest extends EventListenerTest {

    @MockitoBean
    private OffersModule module;

    @Autowired
    public OffersMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToCreateOffers(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(any())).thenReturn(Mono.empty());
        when(module.updateOfferInLookup(any())).thenReturn(Mono.empty());
        when(module.removeOfferFromLookup(any())).thenReturn(Mono.empty());
        when(module.allowUserToManageOffer(any(), any())).thenReturn(Mono.empty());
        when(module.removePermissionsOnOffer(any())).thenReturn(Mono.empty());
        when(module.allowAnonymousAndSystemUsersToReadPublishedOffer(any())).thenReturn(Mono.empty());
        when(module.disallowAnonymousAndSystemUsersToReadPublishedOffer(any())).thenReturn(Mono.empty());
        when(module.updateProductInLookup(any(), anyLong(), any(), any(), any(), any())).thenReturn(Mono.empty());
        when(module.updateProductImagesInLookup(any(), anyLong(), any())).thenReturn(Mono.empty());
        when(module.removeProductFromLookup(any())).thenReturn(Mono.empty());
        when(module.addProductLinkInLookup(any(), anyLong(), any())).thenReturn(Mono.empty());
        when(module.removeProductLinkFromLookup(any(), anyLong(), any(), any())).thenReturn(Mono.empty());
        when(module.updateProductLinkInLookup(any(), anyLong(), any())).thenReturn(Mono.empty());
        when(module.updateProductFabricCompositionInLookup(any(), anyLong(), any())).thenReturn(Mono.empty());
        when(module.updateProductNumberInLookup(any(), anyLong(), any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToCreateOffers() {
        send(
                AggregateType.of("USER"),
                AggregateId.of("USER_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).allowUserToCreateOffers(eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForUser() {
        send(
                AggregateType.of("USER"),
                AggregateId.of("USER_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).removePermissionsForUser(eq("USER_ID"));
    }

    @Test
    void shouldUpdateOfferLookupOnCreated() {
        send(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.user(AgentId.of("USER_ID")),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).updateOfferInLookup(eq("OFFER_ID"));
    }

    @Test
    void shouldRemoveOfferFromLookupOnDeleted() {
        send(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).removeOfferFromLookup(eq("OFFER_ID"));
    }

    @Test
    void shouldAllowUserToManageOfferOnCreated() {
        send(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.user(AgentId.of("USER_ID")),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).allowUserToManageOffer(eq("OFFER_ID"), eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsOnOfferDeleted() {
        send(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).removePermissionsOnOffer(eq("OFFER_ID"));
    }

    @Test
    void shouldAllowAnonymousAndSystemUsersToReadPublishedOffer() {
        send(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(1),
                EventName.of("PUBLISHED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).allowAnonymousAndSystemUsersToReadPublishedOffer(eq("OFFER_ID"));
    }

    @Test
    void shouldDisallowAnonymousAndSystemUsersOnUnpublished() {
        send(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(1),
                EventName.of("UNPUBLISHED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).disallowAnonymousAndSystemUsersToReadPublishedOffer(eq("OFFER_ID"));
    }

    @Test
    void shouldDisallowAnonymousAndSystemUsersOnArchived() {
        send(
                AggregateType.of("OFFER"),
                AggregateId.of("OFFER_ID"),
                Version.of(1),
                EventName.of("ARCHIVED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).disallowAnonymousAndSystemUsersToReadPublishedOffer(eq("OFFER_ID"));
    }

    @Test
    void shouldUpdateProductLookupOnProductCreated() {
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "number", "P-001",
                        "images", List.of("IMAGE_1", "IMAGE_2"),
                        "links", List.of(),
                        "fabricComposition", List.of(
                                Map.of("fabricType", "COTTON", "percentage", 10000L)
                        )
                )
        );

        verify(module, timeout(5000).times(1)).updateProductInLookup(
                eq("PRODUCT_ID"),
                eq(1L),
                eq(ProductNumber.of("P-001")),
                eq(List.of(ImageId.of("IMAGE_1"), ImageId.of("IMAGE_2"))),
                eq(Links.of(Set.of())),
                eq(FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))
                )))
        );
    }

    @Test
    void shouldRemoveProductFromLookupOnProductDeleted() {
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).removeProductFromLookup(eq("PRODUCT_ID"));
    }

    @Test
    void shouldUpdateProductLookupOnProductImagesUpdated() {
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(2),
                EventName.of("IMAGES_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "images", List.of("IMAGE_3", "IMAGE_4")
                )
        );

        verify(module, timeout(5000).times(1)).updateProductImagesInLookup(
                eq("PRODUCT_ID"),
                eq(2L),
                eq(List.of(ImageId.of("IMAGE_3"), ImageId.of("IMAGE_4")))
        );
    }

    @Test
    void shouldUpdateProductLookupOnProductLinkAdded() {
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(2),
                EventName.of("LINK_ADDED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "type", "PATTERN",
                        "id", "PATTERN_ID",
                        "name", "Pattern Name"
                )
        );

        verify(module, timeout(5000).times(1)).addProductLinkInLookup(
                eq("PRODUCT_ID"),
                eq(2L),
                eq(Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern Name")))
        );
    }

    @Test
    void shouldUpdateProductLookupOnProductLinkRemoved() {
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(2),
                EventName.of("LINK_REMOVED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "type", "FABRIC",
                        "id", "FABRIC_ID"
                )
        );

        verify(module, timeout(5000).times(1)).removeProductLinkFromLookup(
                eq("PRODUCT_ID"),
                eq(2L),
                eq(LinkType.FABRIC),
                eq("FABRIC_ID")
        );
    }

    @Test
    void shouldUpdateProductLookupOnProductLinkUpdated() {
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(2),
                EventName.of("LINK_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "type", "PATTERN",
                        "id", "PATTERN_ID",
                        "name", "Updated Name"
                )
        );

        verify(module, timeout(5000).times(1)).updateProductLinkInLookup(
                eq("PRODUCT_ID"),
                eq(2L),
                eq(Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Updated Name")))
        );
    }

    @Test
    void shouldUpdateProductLookupOnFabricCompositionUpdated() {
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(2),
                EventName.of("FABRIC_COMPOSITION_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "fabricComposition", List.of(
                                Map.of("fabricType", "COTTON", "percentage", 10000L)
                        )
                )
        );

        verify(module, timeout(5000).times(1)).updateProductFabricCompositionInLookup(
                eq("PRODUCT_ID"),
                eq(2L),
                eq(FabricComposition.of(Set.of(
                        FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))
                )))
        );
    }

    @Test
    void shouldUpdateProductLookupOnProductNumberUpdated() {
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(2),
                EventName.of("PRODUCT_NUMBER_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "number", "P-999"
                )
        );

        verify(module, timeout(5000).times(1)).updateProductNumberInLookup(
                eq("PRODUCT_ID"),
                eq(2L),
                eq(ProductNumber.of("P-999"))
        );
    }

}
