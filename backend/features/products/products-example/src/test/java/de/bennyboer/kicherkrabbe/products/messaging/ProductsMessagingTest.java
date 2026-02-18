package de.bennyboer.kicherkrabbe.products.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.products.ProductsModule;
import de.bennyboer.kicherkrabbe.products.api.LinkDTO;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.RemoveLinkFromLookupRequest;
import de.bennyboer.kicherkrabbe.products.api.requests.UpdateLinkInLookupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(ProductsMessaging.class)
public class ProductsMessagingTest extends EventListenerTest {

    @MockitoBean
    private ProductsModule module;

    @Autowired
    public ProductsMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToCreateProductsAndReadLinks(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(any())).thenReturn(Mono.empty());
        when(module.updateProductInLookup(any())).thenReturn(Mono.empty());
        when(module.removeProductFromLookup(any())).thenReturn(Mono.empty());
        when(module.removePermissionsOnProduct(any())).thenReturn(Mono.empty());
        when(module.allowUserToReadAndManageProduct(any(), any())).thenReturn(Mono.empty());
        when(module.updateLinkInLookup(any(), any())).thenReturn(Flux.empty());
        when(module.removeLinkFromLookup(any(), any())).thenReturn(Flux.empty());
    }

    @Test
    void shouldAllowUserToCreateProductsAndReadLinks() {
        // when: a user created event is published
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

        // then: the user is allowed to create products and read links
        verify(module, timeout(5000).times(1)).allowUserToCreateProductsAndReadLinks(eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForUser() {
        // when: a user deleted event is published
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

        // then: the permissions for the user are removed
        verify(module, timeout(5000).times(1)).removePermissionsForUser(eq("USER_ID"));
    }

    @Test
    void shouldUpdateProductLookupOnCreated() {
        // when: a product created event is published
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the product is updated in the lookup
        verify(module, timeout(5000).times(1)).updateProductInLookup(eq("PRODUCT_ID"));
    }

    @Test
    void shouldUpdateProductLookupOnLinkAdded() {
        // when: a link added event is published
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("LINK_ADDED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the product is updated in the lookup
        verify(module, timeout(5000).times(1)).updateProductInLookup(eq("PRODUCT_ID"));
    }

    @Test
    void shouldUpdateProductLookupOnLinkRemoved() {
        // when: a link removed event is published
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("LINK_REMOVED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the product is updated in the lookup
        verify(module, timeout(5000).times(1)).updateProductInLookup(eq("PRODUCT_ID"));
    }

    @Test
    void shouldUpdateProductLookupOnLinkUpdated() {
        // when: a link updated event is published
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("LINK_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the product is updated in the lookup
        verify(module, timeout(5000).times(1)).updateProductInLookup(eq("PRODUCT_ID"));
    }

    @Test
    void shouldUpdateProductLookupOnImagesUpdated() {
        // when: an images updated event is published
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("IMAGES_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the product is updated in the lookup
        verify(module, timeout(5000).times(1)).updateProductInLookup(eq("PRODUCT_ID"));
    }

    @Test
    void shouldUpdateProductLookupOnFabricCompositionUpdated() {
        // when: a fabric composition updated event is published
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("FABRIC_COMPOSITION_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the product is updated in the lookup
        verify(module, timeout(5000).times(1)).updateProductInLookup(eq("PRODUCT_ID"));
    }

    @Test
    void shouldUpdateProductLookupOnNotesUpdated() {
        // when: a notes updated event is published
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("NOTES_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the product is updated in the lookup
        verify(module, timeout(5000).times(1)).updateProductInLookup(eq("PRODUCT_ID"));
    }

    @Test
    void shouldUpdateProductLookupOnProducedAtUpdated() {
        // when: a produced at updated event is published
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("PRODUCED_AT_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the product is updated in the lookup
        verify(module, timeout(5000).times(1)).updateProductInLookup(eq("PRODUCT_ID"));
    }

    @Test
    void shouldUpdateProductLookupOnProductNumberUpdated() {
        // when: a product number updated event is published
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("PRODUCT_NUMBER_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the product is updated in the lookup
        verify(module, timeout(5000).times(1)).updateProductInLookup(eq("PRODUCT_ID"));
    }

    @Test
    void shouldRemoveProductFromLookupOnDeleted() {
        // when: a product deleted event is published
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

        // then: the product is removed from the lookup
        verify(module, timeout(5000).times(1)).removeProductFromLookup(eq("PRODUCT_ID"));
    }

    @Test
    void shouldRemovePermissionsForDeletedProduct() {
        // when: a product deleted event is published
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

        // then: the permissions for the product are removed
        verify(module, timeout(5000).times(1)).removePermissionsOnProduct(eq("PRODUCT_ID"));
    }

    @Test
    void shouldAllowUserToReadAndManageProduct() {
        // when: a product created event is published
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.user(AgentId.of("USER_ID")),
                Instant.now(),
                Map.of()
        );

        // then: the user is allowed to read and manage the product
        verify(module, timeout(5000).times(1)).allowUserToReadAndManageProduct(eq("PRODUCT_ID"), eq("USER_ID"));
    }

    @Test
    void shouldUpdateLinkInLookupOnPatternCreated() {
        // when: a pattern created event is published
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "name", "Pattern"
                )
        );

        // then: the link is updated in the lookup
        var request = new UpdateLinkInLookupRequest();
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.PATTERN;
        request.link.id = "PATTERN_ID";
        request.link.name = "Pattern";
        request.version = 1;
        verify(module, timeout(5000).times(1)).updateLinkInLookup(eq(request), eq(Agent.system()));
    }

    @Test
    void shouldUpdateLinkInLookupOnPatternRenamed() {
        // when: a pattern renamed event is published
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("RENAMED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "name", "New name"
                )
        );

        // then: the link is updated in the lookup
        var request = new UpdateLinkInLookupRequest();
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.PATTERN;
        request.link.id = "PATTERN_ID";
        request.link.name = "New name";
        request.version = 1;
        verify(module, timeout(5000).times(1)).updateLinkInLookup(eq(request), eq(Agent.system()));
    }

    @Test
    void shouldRemoveLinkFromLookupOnPatternDeleted() {
        // when: a pattern deleted event is published
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the link is removed from the lookup
        var request = new RemoveLinkFromLookupRequest();
        request.linkType = LinkTypeDTO.PATTERN;
        request.linkId = "PATTERN_ID";
        verify(module, timeout(5000).times(1)).removeLinkFromLookup(eq(request), eq(Agent.system()));
    }

    @Test
    void shouldUpdateLinkInLookupOnFabricCreated() {
        // when: a fabric created event is published
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "name", "Fabric"
                )
        );

        // then: the link is updated in the lookup
        var request = new UpdateLinkInLookupRequest();
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.FABRIC;
        request.link.id = "FABRIC_ID";
        request.link.name = "Fabric";
        request.version = 1;
        verify(module, timeout(5000).times(1)).updateLinkInLookup(eq(request), eq(Agent.system()));
    }

    @Test
    void shouldUpdateLinkInLookupOnFabricRenamed() {
        // when: a fabric renamed event is published
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("RENAMED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "name", "New name"
                )
        );

        // then: the link is updated in the lookup
        var request = new UpdateLinkInLookupRequest();
        request.link = new LinkDTO();
        request.link.type = LinkTypeDTO.FABRIC;
        request.link.id = "FABRIC_ID";
        request.link.name = "New name";
        request.version = 1;
        verify(module, timeout(5000).times(1)).updateLinkInLookup(eq(request), eq(Agent.system()));
    }

    @Test
    void shouldRemoveLinkFromLookupOnFabricDeleted() {
        // when: a fabric deleted event is published
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the link is removed from the lookup
        var request = new RemoveLinkFromLookupRequest();
        request.linkType = LinkTypeDTO.FABRIC;
        request.linkId = "FABRIC_ID";
        verify(module, timeout(5000).times(1)).removeLinkFromLookup(eq(request), eq(Agent.system()));
    }

}
