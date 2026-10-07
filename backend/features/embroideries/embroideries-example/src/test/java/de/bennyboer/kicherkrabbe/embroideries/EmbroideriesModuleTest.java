package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.embroideries.persistence.categories.EmbroideryCategoryRepo;
import de.bennyboer.kicherkrabbe.embroideries.persistence.categories.inmemory.InMemoryEmbroideryCategoryRepo;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.EmbroideryLookupRepo;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.inmemory.InMemoryEmbroideryLookupRepo;
import de.bennyboer.kicherkrabbe.embroideries.samples.SampleEmbroidery;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;
import java.util.Set;

public class EmbroideriesModuleTest {

    private final EmbroideriesModuleConfig config = new EmbroideriesModuleConfig();

    private final EventSourcingRepo eventSourcingRepo = new InMemoryEventSourcingRepo();

    private final EmbroideryService embroideryService = new EmbroideryService(
            eventSourcingRepo,
            new LoggingEventPublisher(),
            Clock.systemUTC()
    );

    private final PermissionsRepo permissionsRepo = new InMemoryPermissionsRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            permissionsRepo,
            event -> Mono.empty()
    );

    private final EmbroideryLookupRepo embroideryLookupRepo = new InMemoryEmbroideryLookupRepo();

    private final ResourceChangesTracker changesTracker = receiverId -> Flux.empty();

    private final EmbroideryCategoryRepo embroideryCategoryRepo = new InMemoryEmbroideryCategoryRepo();

    private final EmbroideriesModule module = config.embroideriesModule(
            embroideryService,
            permissionsService,
            embroideryLookupRepo,
            changesTracker,
            embroideryCategoryRepo
    );

    public List<EmbroideryDetails> getEmbroideries(Agent agent) {
        return getEmbroideries("", Set.of(), 0, Integer.MAX_VALUE, agent).getResults();
    }

    public EmbroideriesPage getEmbroideries(
            String searchTerm,
            Set<String> categories,
            long skip,
            long limit,
            Agent agent
    ) {
        return module.getEmbroideries(searchTerm, categories, skip, limit, agent).block();
    }

    public EmbroideryDetails getEmbroidery(String embroideryId, Agent agent) {
        return module.getEmbroidery(embroideryId, agent).block();
    }

    public PublishedEmbroideriesPage getPublishedEmbroideries(
            String searchTerm,
            Set<String> categories,
            boolean ascending,
            long skip,
            long limit,
            Agent agent
    ) {
        return module.getPublishedEmbroideries(searchTerm, categories, ascending, skip, limit, agent).block();
    }

    public PublishedEmbroidery getPublishedEmbroidery(String embroideryIdOrAlias, Agent agent) {
        return module.getPublishedEmbroidery(embroideryIdOrAlias, agent).block();
    }

    public List<PublishedEmbroidery> getFeaturedEmbroideries(Agent agent) {
        return module.getFeaturedEmbroideries(agent).collectList().block();
    }

    public List<EmbroideryCategory> getAvailableCategoriesForEmbroideries(Agent agent) {
        return module.getAvailableCategoriesForEmbroideries(agent).collectList().block();
    }

    public List<EmbroideryCategory> getCategoriesUsedInEmbroideries(Agent agent) {
        return module.getCategoriesUsedInEmbroideries(agent).collectList().block();
    }

    public String createEmbroidery(String name, String image, Set<String> categories, Agent agent) {
        String embroideryId = module.createEmbroidery(name, image, categories, agent).block();

        module.updateEmbroideryInLookup(embroideryId).block();
        if (agent.getType() == AgentType.USER) {
            module.allowUserToManageEmbroidery(embroideryId, agent.getId().getValue()).block();
        }

        return embroideryId;
    }

    public String createEmbroidery(SampleEmbroidery sample, Agent agent) {
        return createEmbroidery(sample.getName(), sample.getImage(), sample.getCategories(), agent);
    }

    public String createSampleEmbroidery(Agent agent) {
        return createEmbroidery(SampleEmbroidery.builder().build(), agent);
    }

    public String createSampleEmbroidery(Agent agent, String name) {
        return createEmbroidery(SampleEmbroidery.builder().name(name).build(), agent);
    }

    public void renameEmbroidery(String embroideryId, long version, String name, Agent agent) {
        module.renameEmbroidery(embroideryId, version, name, agent).block();

        module.updateEmbroideryInLookup(embroideryId).block();
    }

    public void publishEmbroidery(String embroideryId, long version, Agent agent) {
        module.publishEmbroidery(embroideryId, version, agent).block();

        module.updateEmbroideryInLookup(embroideryId).block();
        module.allowAnonymousAndSystemUsersToReadPublishedEmbroidery(embroideryId).block();
    }

    public void unpublishEmbroidery(String embroideryId, long version, Agent agent) {
        module.unpublishEmbroidery(embroideryId, version, agent).block();

        module.updateEmbroideryInLookup(embroideryId).block();
        module.disallowAnonymousAndSystemUsersToReadPublishedEmbroidery(embroideryId).block();
    }

    public void featureEmbroidery(String embroideryId, long version, Agent agent) {
        module.featureEmbroidery(embroideryId, version, agent).block();

        module.updateEmbroideryInLookup(embroideryId).block();
    }

    public void unfeatureEmbroidery(String embroideryId, long version, Agent agent) {
        module.unfeatureEmbroidery(embroideryId, version, agent).block();

        module.updateEmbroideryInLookup(embroideryId).block();
    }

    public void updateEmbroideryImage(String embroideryId, long version, String image, Agent agent) {
        module.updateEmbroideryImage(embroideryId, version, image, agent).block();

        module.updateEmbroideryInLookup(embroideryId).block();
    }

    public void updateEmbroideryCategories(String embroideryId, long version, Set<String> categories, Agent agent) {
        module.updateEmbroideryCategories(embroideryId, version, categories, agent).block();

        module.updateEmbroideryInLookup(embroideryId).block();
    }

    public void deleteEmbroidery(String embroideryId, long version, Agent agent) {
        module.deleteEmbroidery(embroideryId, version, agent).block();

        module.removeEmbroideryFromLookup(embroideryId).block();
        module.removePermissionsOnEmbroidery(embroideryId).block();
    }

    public void removeCategoryFromEmbroideries(String categoryId) {
        List<String> updatedEmbroideryIds = module.removeCategoryFromEmbroideries(categoryId, Agent.system())
                .collectList()
                .block();

        for (String embroideryId : updatedEmbroideryIds) {
            module.updateEmbroideryInLookup(embroideryId).block();
        }
    }

    public void allowUserToCreateEmbroideries(String userId) {
        module.allowUserToCreateEmbroideries(userId).block();
    }

    public void removePermissionsForUser(String userId) {
        module.removePermissionsForUser(userId).block();
    }

    public void markCategoryAsAvailable(String id, String name) {
        module.markCategoryAsAvailable(id, name).block();
    }

    public void markCategoryAsUnavailable(String id) {
        module.markCategoryAsUnavailable(id).block();
    }

    public void renameCategoryIfAvailable(String id, String name) {
        module.renameCategoryIfAvailable(id, name).block();
    }

}
