package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsAvailabilityFilterDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsSortDTO;
import de.bennyboer.kicherkrabbe.fabrics.samples.SampleFabric;
import de.bennyboer.kicherkrabbe.fabrics.samples.SampleFabricTypeAvailability;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.Color;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.ColorRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.inmemory.InMemoryColorRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricType;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricTypeRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.inmemory.InMemoryFabricTypeRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.FabricLookupRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.inmemory.InMemoryFabricLookupRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.Topic;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.TopicRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.inmemory.InMemoryTopicRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;
import java.util.Set;

public class FabricsModuleTest {

    protected FabricTypeAvailabilityDTO jerseyAvailability = SampleFabricTypeAvailability.builder()
            .typeId("JERSEY_ID")
            .inStock(true)
            .build()
            .toDTO();

    protected FabricTypeAvailabilityDTO cottonAvailability = SampleFabricTypeAvailability.builder()
            .typeId("COTTON_ID")
            .inStock(false)
            .build()
            .toDTO();

    private final FabricsModuleConfig config = new FabricsModuleConfig();

    private final EventSourcingRepo eventSourcingRepo = new InMemoryEventSourcingRepo();

    private final FabricService fabricService = new FabricService(
            eventSourcingRepo,
            new LoggingEventPublisher(),
            Clock.systemUTC()
    );

    private final PermissionsRepo permissionsRepo = new InMemoryPermissionsRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            permissionsRepo,
            event -> Mono.empty()
    );

    private final FabricLookupRepo fabricLookupRepo = new InMemoryFabricLookupRepo();

    private final ResourceChangesTracker changesTracker = receiverId -> Flux.empty();

    private final TopicRepo topicRepo = new InMemoryTopicRepo();

    private final ColorRepo colorRepo = new InMemoryColorRepo();

    private final FabricTypeRepo fabricTypeRepo = new InMemoryFabricTypeRepo();

    private final FabricsModule module = config.fabricsModule(
            fabricService,
            permissionsService,
            fabricLookupRepo,
            changesTracker,
            topicRepo,
            colorRepo,
            fabricTypeRepo
    );

    public List<FabricDetails> getFabrics(Agent agent) {
        return getFabrics("", 0, Integer.MAX_VALUE, agent).getResults();
    }

    public FabricsPage getFabrics(String searchTerm, long skip, long limit, Agent agent) {
        return module.getFabrics(searchTerm, skip, limit, agent).block();
    }

    public String createFabric(
            String name,
            String imageId,
            Set<String> colorIds,
            Set<String> topicIds,
            Set<FabricTypeAvailabilityDTO> availability,
            Agent agent
    ) {
        String fabricId = module.createFabric(name, imageId, colorIds, topicIds, availability, agent).block();

        module.updateFabricInLookup(fabricId).block();
        if (agent.getType() == AgentType.USER) {
            module.allowUserToManageFabric(fabricId, agent.getId().getValue()).block();
        }

        return fabricId;
    }

    public String createFabric(SampleFabric sample, Agent agent) {
        return createFabric(
                sample.getName(),
                sample.getImageId(),
                sample.getColorIds(),
                sample.getTopicIds(),
                sample.getAvailabilityDTOs(),
                agent
        );
    }

    public String createSampleFabric(Agent agent) {
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        return createFabric(SampleFabric.builder().build(), agent);
    }

    public String createSampleFabric(Agent agent, String name) {
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        return createFabric(SampleFabric.builder().name(name).build(), agent);
    }

    public void deleteFabric(String fabricId, long version, Agent agent) {
        module.deleteFabric(fabricId, version, agent).block();

        module.removeFabricFromLookup(fabricId).block();
        module.removePermissionsForUser(fabricId).block();
    }

    public void renameFabric(String fabricId, long version, String name, Agent agent) {
        module.renameFabric(fabricId, version, name, agent).block();

        module.updateFabricInLookup(fabricId).block();
    }

    public void publishFabric(String fabricId, long version, Agent agent) {
        module.publishFabric(fabricId, version, agent).block();

        module.updateFabricInLookup(fabricId).block();
        module.allowAnonymousAndSystemUsersToReadPublishedFabric(fabricId).block();
    }

    public void unpublishFabric(String fabricId, long version, Agent agent) {
        module.unpublishFabric(fabricId, version, agent).block();

        module.updateFabricInLookup(fabricId).block();
        module.disallowAnonymousAndSystemUsersToReadPublishedFabric(fabricId).block();
    }

    public void featureFabric(String fabricId, long version, Agent agent) {
        module.featureFabric(fabricId, version, agent).block();

        module.updateFabricInLookup(fabricId).block();
    }

    public void unfeatureFabric(String fabricId, long version, Agent agent) {
        module.unfeatureFabric(fabricId, version, agent).block();

        module.updateFabricInLookup(fabricId).block();
    }

    public void updateFabricImage(String fabricId, long version, String imageId, Agent agent) {
        module.updateFabricImage(fabricId, version, imageId, agent).block();

        module.updateFabricInLookup(fabricId).block();
    }

    public void updateFabricColors(String fabricId, long version, Set<String> colorIds, Agent agent) {
        module.updateFabricColors(fabricId, version, colorIds, agent).block();

        module.updateFabricInLookup(fabricId).block();
    }

    public void updateFabricTopics(String fabricId, long version, Set<String> topicIds, Agent agent) {
        module.updateFabricTopics(fabricId, version, topicIds, agent).block();

        module.updateFabricInLookup(fabricId).block();
    }

    public void updateFabricAvailability(
            String fabricId,
            long version,
            Set<FabricTypeAvailabilityDTO> availability,
            Agent agent
    ) {
        module.updateFabricAvailability(fabricId, version, availability, agent).block();

        module.updateFabricInLookup(fabricId).block();
    }

    public FabricDetails getFabric(String fabricId, Agent agent) {
        return module.getFabric(fabricId, agent).block();
    }

    public PublishedFabric getPublishedFabric(String fabricId, Agent agent) {
        return module.getPublishedFabric(fabricId, agent).block();
    }

    public PublishedFabricsPage getPublishedFabrics(
            String searchTerm,
            Set<String> colorIds,
            Set<String> topicIds,
            FabricsAvailabilityFilterDTO availability,
            FabricsSortDTO sort,
            long skip,
            long limit,
            Agent agent
    ) {
        return module.getPublishedFabrics(
                searchTerm,
                colorIds,
                topicIds,
                availability,
                sort,
                skip,
                limit,
                agent
        ).block();
    }

    public List<PublishedFabric> getFeaturedFabrics(Agent agent) {
        return module.getFeaturedFabrics(agent).collectList().block();
    }

    public void removeTopicFromFabrics(String topicId) {
        List<String> updatedFabricIds = module.removeTopicFromFabrics(topicId, Agent.system()).collectList().block();

        for (String fabricId : updatedFabricIds) {
            module.updateFabricInLookup(fabricId).block();
        }
    }

    public void removeFabricTypeFromFabrics(String fabricTypeId) {
        List<String> updatedFabricIds = module.removeFabricTypeFromFabrics(fabricTypeId, Agent.system())
                .collectList()
                .block();

        for (String fabricId : updatedFabricIds) {
            module.updateFabricInLookup(fabricId).block();
        }
    }

    public void removeColorFromFabrics(String colorId) {
        List<String> updatedFabricIds = module.removeColorFromFabrics(colorId, Agent.system()).collectList().block();

        for (String fabricId : updatedFabricIds) {
            module.updateFabricInLookup(fabricId).block();
        }
    }

    public List<Topic> getAvailableTopicsForFabrics(Agent agent) {
        return module.getAvailableTopicsForFabrics(agent).collectList().block();
    }

    public List<Topic> getTopicsUsedInFabrics(Agent agent) {
        return module.getTopicsUsedInFabrics(agent).collectList().block();
    }

    public List<Color> getAvailableColorsForFabrics(Agent agent) {
        return module.getAvailableColorsForFabrics(agent).collectList().block();
    }

    public List<Color> getColorsUsedInFabrics(Agent agent) {
        return module.getColorsUsedInFabrics(agent).collectList().block();
    }

    public List<FabricType> getAvailableFabricTypesForFabrics(Agent agent) {
        return module.getAvailableFabricTypesForFabrics(agent).collectList().block();
    }

    public List<FabricType> getFabricTypesUsedInFabrics(Agent agent) {
        return module.getFabricTypesUsedInFabrics(agent).collectList().block();
    }

    public void allowUserToCreateFabrics(String userId) {
        module.allowUserToCreateFabrics(userId).block();
    }

    public void allowUserToReadFabric(String userId, String fabricId) {
        module.allowUserToManageFabric(fabricId, userId).block();
    }

    public void markTopicAsAvailable(String id, String name) {
        module.markTopicAsAvailable(id, name).block();
    }

    public void markTopicAsUnavailable(String id) {
        module.markTopicAsUnavailable(id).block();
    }

    public void markColorAsAvailable(String id, String name, int red, int green, int blue) {
        module.markColorAsAvailable(id, name, red, green, blue).block();
    }

    public void markColorAsUnavailable(String id) {
        module.markColorAsUnavailable(id).block();
    }

    public void markFabricTypeAsAvailable(String id, String name) {
        module.markFabricTypeAsAvailable(id, name).block();
    }

    public void markFabricTypeAsUnavailable(String id) {
        module.markFabricTypeAsUnavailable(id).block();
    }

}
