package de.bennyboer.kicherkrabbe.highlights.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.highlights.HighlightsModule;
import de.bennyboer.kicherkrabbe.highlights.api.LinkDTO;
import de.bennyboer.kicherkrabbe.highlights.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.highlights.api.requests.RemoveLinkFromLookupRequest;
import de.bennyboer.kicherkrabbe.highlights.api.requests.UpdateLinkInLookupRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class HighlightsMessaging {

    @Bean
    public EventListener onUserCreatedAllowUserToCreateHighlightsAndReadLinks(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.user-created-allow-user-to-create-highlights-and-read-links",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateHighlightsAndReadLinks(userId);
                }
        );
    }

    @Bean
    public EventListener onUserDeletedRemoveHighlightsPermissionsForUser(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean
    public EventListener onHighlightCreatedUpdateLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.highlight-created-update-lookup",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("CREATED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    return module.updateHighlightInLookup(highlightId);
                }
        );
    }

    @Bean
    public EventListener onHighlightImageUpdatedUpdateLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.highlight-image-updated-update-lookup",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("IMAGE_UPDATED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    return module.updateHighlightInLookup(highlightId);
                }
        );
    }

    @Bean
    public EventListener onHighlightLinkAddedUpdateLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.highlight-link-added-update-lookup",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("LINK_ADDED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    return module.updateHighlightInLookup(highlightId);
                }
        );
    }

    @Bean
    public EventListener onHighlightLinkUpdatedUpdateLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.highlight-link-updated-update-lookup",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("LINK_UPDATED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    return module.updateHighlightInLookup(highlightId);
                }
        );
    }

    @Bean
    public EventListener onHighlightLinkRemovedUpdateLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.highlight-link-removed-update-lookup",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("LINK_REMOVED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    return module.updateHighlightInLookup(highlightId);
                }
        );
    }

    @Bean
    public EventListener onHighlightPublishedUpdateLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.highlight-published-update-lookup",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("PUBLISHED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    return module.updateHighlightInLookup(highlightId);
                }
        );
    }

    @Bean
    public EventListener onHighlightUnpublishedUpdateLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.highlight-unpublished-update-lookup",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("UNPUBLISHED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    return module.updateHighlightInLookup(highlightId);
                }
        );
    }

    @Bean
    public EventListener onHighlightSortOrderUpdatedUpdateLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.highlight-sort-order-updated-update-lookup",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("SORT_ORDER_UPDATED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    return module.updateHighlightInLookup(highlightId);
                }
        );
    }

    @Bean
    public EventListener onHighlightDeletedRemoveHighlightFromLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.highlight-deleted-remove-highlight-from-lookup",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("DELETED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    return module.removeHighlightFromLookup(highlightId);
                }
        );
    }

    @Bean
    public EventListener onHighlightCreatedAllowUserToManageHighlight(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.highlight-created-allow-user-to-manage-highlight",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("CREATED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    Agent agent = event.getMetadata().getAgent();
                    if (agent.getType() == AgentType.USER) {
                        String userId = agent.getId().getValue();
                        return module.allowUserToManageHighlight(highlightId, userId);
                    }

                    return Mono.empty();
                }
        );
    }

    @Bean
    public EventListener onHighlightDeletedRemovePermissionsForHighlight(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.highlight-deleted-remove-permissions",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("DELETED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForHighlight(highlightId);
                }
        );
    }

    @Bean
    public EventListener onPatternCreatedUpdateLinkInLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.pattern-created-update-link-in-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("CREATED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();
                    long version = event.getMetadata().getAggregateVersion().getValue();

                    var request = new UpdateLinkInLookupRequest();
                    request.link = new LinkDTO();
                    request.link.type = LinkTypeDTO.PATTERN;
                    request.link.id = patternId;
                    request.link.name = (String) event.getEvent().get("name");
                    request.version = version;
                    return module.updateLinkInLookup(request, Agent.system()).then();
                }
        );
    }

    @Bean
    public EventListener onPatternRenamedUpdateLinkInLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.pattern-renamed-update-link-in-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("RENAMED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();
                    long version = event.getMetadata().getAggregateVersion().getValue();

                    var request = new UpdateLinkInLookupRequest();
                    request.link = new LinkDTO();
                    request.link.type = LinkTypeDTO.PATTERN;
                    request.link.id = patternId;
                    request.link.name = (String) event.getEvent().get("name");
                    request.version = version;
                    return module.updateLinkInLookup(request, Agent.system()).then();
                }
        );
    }

    @Bean
    public EventListener onPatternDeletedRemoveLinkFromLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.pattern-deleted-remove-link-from-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("DELETED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    var request = new RemoveLinkFromLookupRequest();
                    request.linkType = LinkTypeDTO.PATTERN;
                    request.linkId = patternId;
                    return module.removeLinkFromLookup(request, Agent.system());
                }
        );
    }

    @Bean
    public EventListener onFabricCreatedUpdateLinkInLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.fabric-created-update-link-in-lookup",
                AggregateType.of("FABRIC"),
                EventName.of("CREATED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();
                    long version = event.getMetadata().getAggregateVersion().getValue();

                    var request = new UpdateLinkInLookupRequest();
                    request.link = new LinkDTO();
                    request.link.type = LinkTypeDTO.FABRIC;
                    request.link.id = fabricId;
                    request.link.name = (String) event.getEvent().get("name");
                    request.version = version;
                    return module.updateLinkInLookup(request, Agent.system()).then();
                }
        );
    }

    @Bean
    public EventListener onFabricRenamedUpdateLinkInLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.fabric-renamed-update-link-in-lookup",
                AggregateType.of("FABRIC"),
                EventName.of("RENAMED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();
                    long version = event.getMetadata().getAggregateVersion().getValue();

                    var request = new UpdateLinkInLookupRequest();
                    request.link = new LinkDTO();
                    request.link.type = LinkTypeDTO.FABRIC;
                    request.link.id = fabricId;
                    request.link.name = (String) event.getEvent().get("name");
                    request.version = version;
                    return module.updateLinkInLookup(request, Agent.system()).then();
                }
        );
    }

    @Bean
    public EventListener onFabricDeletedRemoveLinkFromLookup(
            EventListenerFactory factory,
            HighlightsModule module
    ) {
        return factory.createEventListenerForEvent(
                "highlights.fabric-deleted-remove-link-from-lookup",
                AggregateType.of("FABRIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    var request = new RemoveLinkFromLookupRequest();
                    request.linkType = LinkTypeDTO.FABRIC;
                    request.linkId = fabricId;
                    return module.removeLinkFromLookup(request, Agent.system());
                }
        );
    }

}
