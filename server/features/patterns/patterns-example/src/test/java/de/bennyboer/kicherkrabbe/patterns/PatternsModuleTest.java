package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternExtraDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternVariantDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternsSortDTO;
import de.bennyboer.kicherkrabbe.patterns.persistence.categories.PatternCategoryRepo;
import de.bennyboer.kicherkrabbe.patterns.persistence.categories.inmemory.InMemoryPatternCategoryRepo;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.PatternLookupRepo;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.inmemory.InMemoryPatternLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public class PatternsModuleTest {

    private final PatternsModuleConfig config = new PatternsModuleConfig();

    private final EventSourcingRepo eventSourcingRepo = new InMemoryEventSourcingRepo();

    private final PatternService patternService = new PatternService(eventSourcingRepo, new LoggingEventPublisher());

    private final PermissionsRepo permissionsRepo = new InMemoryPermissionsRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            permissionsRepo,
            event -> Mono.empty()
    );

    private final PatternLookupRepo patternLookupRepo = new InMemoryPatternLookupRepo();

    private final ResourceChangesTracker changesTracker = receiverId -> Flux.empty();

    private final PatternCategoryRepo patternCategoryRepo = new InMemoryPatternCategoryRepo();

    private final PatternsModule module = config.patternsModule(
            patternService,
            permissionsService,
            patternLookupRepo,
            changesTracker,
            patternCategoryRepo
    );

    public List<PatternDetails> getPatterns(Agent agent) {
        return getPatterns("", Set.of(), 0, Integer.MAX_VALUE, agent).getResults();
    }

    public PatternsPage getPatterns(String searchTerm, Set<String> categories, long skip, long limit, Agent agent) {
        return module.getPatterns(searchTerm, categories, skip, limit, agent).block();
    }

    public String createPattern(
            String name,
            String number,
            @Nullable String description,
            PatternAttributionDTO attribution,
            Set<String> categories,
            List<String> images,
            List<PatternVariantDTO> variants,
            List<PatternExtraDTO> extras,
            Agent agent
    ) {
        String patternId = module.createPattern(
                name,
                number,
                description,
                attribution,
                categories,
                images,
                variants,
                extras,
                agent
        ).block();

        module.updatePatternInLookup(patternId).block();
        if (agent.getType() == AgentType.USER) {
            module.allowUserToManagePattern(patternId, agent.getId().getValue()).block();
        }

        return patternId;
    }

    public void deletePattern(String patternId, long version, Agent agent) {
        module.deletePattern(patternId, version, agent).block();

        module.removePatternFromLookup(patternId).block();
        module.removePermissionsForUser(patternId).block();
    }

    public void renamePattern(String patternId, long version, String name, Agent agent) {
        module.renamePattern(patternId, version, name, agent).block();

        module.updatePatternInLookup(patternId).block();
    }

    public void publishPattern(String patternId, long version, Agent agent) {
        module.publishPattern(patternId, version, agent).block();

        module.updatePatternInLookup(patternId).block();
        module.allowAnonymousAndSystemUsersToReadPublishedPattern(patternId).block();
    }

    public void unpublishPattern(String patternId, long version, Agent agent) {
        module.unpublishPattern(patternId, version, agent).block();

        module.updatePatternInLookup(patternId).block();
        module.disallowAnonymousAndSystemUsersToReadPublishedPattern(patternId).block();
    }

    public void updatePatternAttribution(
            String patternId,
            long version,
            PatternAttributionDTO attribution,
            Agent agent
    ) {
        module.updatePatternAttribution(patternId, version, attribution, agent).block();

        module.updatePatternInLookup(patternId).block();
    }

    public void updatePatternCategories(String patternId, long version, Set<String> categories, Agent agent) {
        module.updatePatternCategories(patternId, version, categories, agent).block();

        module.updatePatternInLookup(patternId).block();
    }

    public void updatePatternImages(String patternId, long version, List<String> images, Agent agent) {
        module.updatePatternImages(patternId, version, images, agent).block();

        module.updatePatternInLookup(patternId).block();
    }

    public void updatePatternVariants(String patternId, long version, List<PatternVariantDTO> variants, Agent agent) {
        module.updatePatternVariants(patternId, version, variants, agent).block();

        module.updatePatternInLookup(patternId).block();
    }

    public void updatePatternExtras(String patternId, long version, List<PatternExtraDTO> extras, Agent agent) {
        module.updatePatternExtras(patternId, version, extras, agent).block();

        module.updatePatternInLookup(patternId).block();
    }

    public void updatePatternDescription(String patternId, long version, @Nullable String description, Agent agent) {
        module.updatePatternDescription(patternId, version, description, agent).block();

        module.updatePatternInLookup(patternId).block();
    }

    public void updatePatternNumber(String patternId, long version, String number, Agent agent) {
        module.updatePatternNumber(patternId, version, number, agent).block();

        module.updatePatternInLookup(patternId).block();
    }

    public PatternDetails getPattern(String patternId, Agent agent) {
        return module.getPattern(patternId, agent).block();
    }

    public PublishedPattern getPublishedPattern(String patternId, Agent agent) {
        return module.getPublishedPattern(patternId, agent).block();
    }

    public PublishedPatternsPage getPublishedPatterns(
            String searchTerm,
            Set<String> categories,
            Set<Long> sizes,
            PatternsSortDTO sort,
            long skip,
            long limit,
            Agent agent
    ) {
        return module.getPublishedPatterns(
                searchTerm,
                categories,
                sizes,
                sort,
                skip,
                limit,
                agent
        ).block();
    }

    public void allowUserToCreatePatterns(String userId) {
        module.allowUserToCreatePatterns(userId).block();
    }

    public void allowUserToReadPattern(String userId, String patternId) {
        module.allowUserToManagePattern(patternId, userId).block();
    }

    public List<PatternCategory> getAvailableCategoriesForPatterns(Agent agent) {
        return module.getAvailableCategoriesForPatterns(agent).collectList().block();
    }

    public List<PatternCategory> getCategoriesUsedInPatterns(Agent agent) {
        return module.getCategoriesUsedInPatterns(agent).collectList().block();
    }

    public void markCategoryAsAvailable(String id, String name) {
        module.markCategoryAsAvailable(id, name).block();
    }

    public void markCategoryAsUnavailable(String id) {
        module.markCategoryAsUnavailable(id).block();
    }

    public void removeCategoryFromPatterns(String categoryId) {
        List<String> updatedPatternIds = module.removeCategoryFromPatterns(categoryId, Agent.system())
                .collectList()
                .block();

        for (String patternId : updatedPatternIds) {
            module.updatePatternInLookup(patternId).block();
        }
    }

}
