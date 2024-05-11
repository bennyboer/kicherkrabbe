package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.fabrics.http.requests.FabricTypeAvailabilityDTO;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.FabricLookupRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.inmemory.InMemoryFabricLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public class FabricsModuleTest {

    protected FabricTypeAvailabilityDTO jerseyAvailability = new FabricTypeAvailabilityDTO();

    {
        jerseyAvailability.typeId = "JERSEY_ID";
        jerseyAvailability.inStock = true;
    }

    protected FabricTypeAvailabilityDTO cottonAvailability = new FabricTypeAvailabilityDTO();

    {
        cottonAvailability.typeId = "COTTON_ID";
        cottonAvailability.inStock = false;
    }

    private final FabricsModuleConfig config = new FabricsModuleConfig();

    private final EventSourcingRepo eventSourcingRepo = new InMemoryEventSourcingRepo();

    private final FabricService fabricService = new FabricService(eventSourcingRepo, new LoggingEventPublisher());

    private final PermissionsRepo permissionsRepo = new InMemoryPermissionsRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            permissionsRepo,
            event -> Mono.empty()
    );

    private final FabricLookupRepo fabricLookupRepo = new InMemoryFabricLookupRepo();

    private final FabricsModule module = config.fabricsModule(fabricService, permissionsService, fabricLookupRepo);

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
    }

    public void unpublishFabric(String fabricId, long version, Agent agent) {
        module.unpublishFabric(fabricId, version, agent).block();

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

    public void allowUserToCreateFabrics(String userId) {
        module.allowUserToCreateFabrics(userId).block();
    }

}
