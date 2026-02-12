package de.bennyboer.kicherkrabbe.highlights.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.highlights.HighlightsModule;
import de.bennyboer.kicherkrabbe.highlights.api.LinkDTO;
import de.bennyboer.kicherkrabbe.highlights.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.highlights.api.requests.RemoveLinkFromLookupRequest;
import de.bennyboer.kicherkrabbe.highlights.api.requests.UpdateLinkInLookupRequest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
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

@Import(HighlightsMessaging.class)
public class HighlightsMessagingTest extends EventListenerTest {

    @MockitoBean
    private HighlightsModule module;

    @Autowired
    public HighlightsMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToCreateHighlightsAndReadLinks(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(any())).thenReturn(Mono.empty());
        when(module.updateHighlightInLookup(any())).thenReturn(Mono.empty());
        when(module.removeHighlightFromLookup(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForHighlight(any())).thenReturn(Mono.empty());
        when(module.allowUserToManageHighlight(any(), any())).thenReturn(Mono.empty());
        when(module.updateLinkInLookup(any(), any())).thenReturn(Flux.empty());
        when(module.removeLinkFromLookup(any(), any())).thenReturn(Flux.empty());
    }

    @Test
    void shouldAllowUserToCreateHighlightsAndReadLinks() {
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

        verify(module, timeout(5000).times(1)).allowUserToCreateHighlightsAndReadLinks(eq("USER_ID"));
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
    void shouldUpdateHighlightLookupOnCreated() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).updateHighlightInLookup(eq("HIGHLIGHT_ID"));
    }

    @Test
    void shouldUpdateHighlightLookupOnImageUpdated() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("IMAGE_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).updateHighlightInLookup(eq("HIGHLIGHT_ID"));
    }

    @Test
    void shouldUpdateHighlightLookupOnLinkAdded() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("LINK_ADDED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).updateHighlightInLookup(eq("HIGHLIGHT_ID"));
    }

    @Test
    void shouldUpdateHighlightLookupOnLinkUpdated() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("LINK_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).updateHighlightInLookup(eq("HIGHLIGHT_ID"));
    }

    @Test
    void shouldUpdateHighlightLookupOnLinkRemoved() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("LINK_REMOVED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).updateHighlightInLookup(eq("HIGHLIGHT_ID"));
    }

    @Test
    void shouldUpdateHighlightLookupOnPublished() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("PUBLISHED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).updateHighlightInLookup(eq("HIGHLIGHT_ID"));
    }

    @Test
    void shouldUpdateHighlightLookupOnUnpublished() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("UNPUBLISHED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).updateHighlightInLookup(eq("HIGHLIGHT_ID"));
    }

    @Test
    void shouldUpdateHighlightLookupOnSortOrderUpdated() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("SORT_ORDER_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).updateHighlightInLookup(eq("HIGHLIGHT_ID"));
    }

    @Test
    void shouldRemoveHighlightFromLookupOnDeleted() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).removeHighlightFromLookup(eq("HIGHLIGHT_ID"));
    }

    @Test
    void shouldRemovePermissionsForDeletedHighlight() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).removePermissionsForHighlight(eq("HIGHLIGHT_ID"));
    }

    @Test
    void shouldAllowUserToManageHighlight() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.user(AgentId.of("USER_ID")),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).allowUserToManageHighlight(eq("HIGHLIGHT_ID"), eq("USER_ID"));
    }

    @Test
    void shouldUpdateLinkInLookupOnPatternCreated() {
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "Pattern")
        );

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
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("RENAMED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "New name")
        );

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

        var request = new RemoveLinkFromLookupRequest();
        request.linkType = LinkTypeDTO.PATTERN;
        request.linkId = "PATTERN_ID";
        verify(module, timeout(5000).times(1)).removeLinkFromLookup(eq(request), eq(Agent.system()));
    }

    @Test
    void shouldUpdateLinkInLookupOnFabricCreated() {
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "Fabric")
        );

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
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("RENAMED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "New name")
        );

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

        var request = new RemoveLinkFromLookupRequest();
        request.linkType = LinkTypeDTO.FABRIC;
        request.linkId = "FABRIC_ID";
        verify(module, timeout(5000).times(1)).removeLinkFromLookup(eq(request), eq(Agent.system()));
    }

}
