package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.highlights.api.requests.RemoveLinkFromLookupRequest;
import de.bennyboer.kicherkrabbe.highlights.api.requests.UpdateLinkInLookupRequest;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.HighlightLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.inmemory.InMemoryHighlightLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.LinkLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.inmemory.InMemoryLinkLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class HighlightsModuleTest {

    private final HighlightService highlightService = new HighlightService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            Clock.systemUTC()
    );

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    private final HighlightLookupRepo highlightLookupRepo = new InMemoryHighlightLookupRepo();

    private final LinkLookupRepo linkLookupRepo = new InMemoryLinkLookupRepo();

    private final ReactiveTransactionManager transactionManager = new MockReactiveTransactionManager();

    private final HighlightsModule module = new HighlightsModule(
            highlightService,
            permissionsService,
            highlightLookupRepo,
            linkLookupRepo,
            agent -> Flux.empty(),
            transactionManager
    );

    public void allowUserToCreateHighlightsAndReadLinks(String userId) {
        module.allowUserToCreateHighlightsAndReadLinks(userId).block();
    }

    public String createHighlight(String imageId, long sortOrder, Agent agent) {
        String highlightId = module.createHighlight(imageId, sortOrder, agent).block();

        module.updateHighlightInLookup(highlightId).block();
        if (agent.getType() == AgentType.USER) {
            module.allowUserToManageHighlight(highlightId, agent.getId().getValue()).block();
        }

        return highlightId;
    }

    public long updateImage(String highlightId, long version, String imageId, Agent agent) {
        var updatedVersion = module.updateImage(highlightId, version, imageId, agent).block();

        module.updateHighlightInLookup(highlightId).block();

        return updatedVersion;
    }

    public long addLink(String highlightId, long version, LinkType linkType, String linkId, String linkName, Agent agent) {
        var updatedVersion = module.addLink(highlightId, version, linkType, linkId, linkName, agent).block();

        module.updateHighlightInLookup(highlightId).block();

        return updatedVersion;
    }

    public long removeLink(String highlightId, long version, LinkType linkType, String linkId, Agent agent) {
        var updatedVersion = module.removeLink(highlightId, version, linkType, linkId, agent).block();

        module.updateHighlightInLookup(highlightId).block();

        return updatedVersion;
    }

    public long publishHighlight(String highlightId, long version, Agent agent) {
        var updatedVersion = module.publishHighlight(highlightId, version, agent).block();

        module.updateHighlightInLookup(highlightId).block();

        return updatedVersion;
    }

    public long unpublishHighlight(String highlightId, long version, Agent agent) {
        var updatedVersion = module.unpublishHighlight(highlightId, version, agent).block();

        module.updateHighlightInLookup(highlightId).block();

        return updatedVersion;
    }

    public long updateSortOrder(String highlightId, long version, long sortOrder, Agent agent) {
        var updatedVersion = module.updateSortOrder(highlightId, version, sortOrder, agent).block();

        module.updateHighlightInLookup(highlightId).block();

        return updatedVersion;
    }

    public long deleteHighlight(String highlightId, long version, Agent agent) {
        var updatedVersion = module.deleteHighlight(highlightId, version, agent).block();

        module.removeHighlightFromLookup(highlightId).block();
        module.removePermissionsForHighlight(highlightId).block();

        return updatedVersion;
    }

    public List<HighlightDetails> getHighlights(Agent agent) {
        return getHighlights(0, Integer.MAX_VALUE, agent);
    }

    public List<HighlightDetails> getHighlights(long skip, long limit, Agent agent) {
        return module.getHighlights(skip, limit, agent).block().getResults();
    }

    public HighlightDetails getHighlight(String highlightId, Agent agent) {
        return module.getHighlight(highlightId, agent).block();
    }

    public List<HighlightDetails> getPublishedHighlights() {
        return module.getPublishedHighlights().collectList().block();
    }

    public List<String> updateLinkInLookup(UpdateLinkInLookupRequest request) {
        module.initialize().block();

        List<String> updatedHighlightIds = module.updateLinkInLookup(request, Agent.system())
                .collectList()
                .block();

        for (String highlightId : updatedHighlightIds) {
            module.updateHighlightInLookup(highlightId).block();
        }

        return updatedHighlightIds;
    }

    public List<String> removeLinkFromLookup(RemoveLinkFromLookupRequest request) {
        module.initialize().block();

        List<String> updatedHighlightIds = module.removeLinkFromLookup(request, Agent.system())
                .collectList()
                .block();

        for (String highlightId : updatedHighlightIds) {
            module.updateHighlightInLookup(highlightId).block();
        }

        return updatedHighlightIds;
    }

}
