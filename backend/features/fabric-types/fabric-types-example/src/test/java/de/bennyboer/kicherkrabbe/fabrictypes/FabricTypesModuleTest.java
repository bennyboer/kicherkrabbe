package de.bennyboer.kicherkrabbe.fabrictypes;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.fabrictypes.samples.SampleFabricType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.FabricTypeLookupRepo;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.inmemory.InMemoryFabricTypeLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class FabricTypesModuleTest {

    private final FabricTypesModuleConfig config = new FabricTypesModuleConfig();

    private final FabricTypeService fabricTypeService = new FabricTypeService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            Clock.systemUTC()
    );

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    private final FabricTypeLookupRepo fabricTypeLookupRepo = new InMemoryFabricTypeLookupRepo();

    private final FabricTypesModule module = config.fabricTypesModule(
            fabricTypeService,
            permissionsService,
            fabricTypeLookupRepo,
            agent -> Flux.empty()
    );

    public void allowUserToCreateFabricTypes(String userId) {
        module.allowUserToCreateFabricTypes(userId).block();
    }

    public String createFabricType(String name, Agent agent) {
        String fabricTypeId = module.createFabricType(name, agent).block();

        module.updateFabricTypeInLookup(fabricTypeId).block();
        if (agent.getType() == AgentType.USER) {
            module.allowUserToManageFabricType(fabricTypeId, agent.getId().getValue()).block();
        }

        return fabricTypeId;
    }

    public String createFabricType(SampleFabricType sample, Agent agent) {
        return createFabricType(sample.getName(), agent);
    }

    public String createSampleFabricType(Agent agent) {
        return createFabricType(SampleFabricType.builder().build(), agent);
    }

    public long updateFabricType(String fabricTypeId, long version, String name, Agent agent) {
        var updatedVersion = module.updateFabricType(fabricTypeId, version, name, agent).block();

        module.updateFabricTypeInLookup(fabricTypeId).block();

        return updatedVersion;
    }

    public long deleteFabricType(String fabricTypeId, long version, Agent agent) {
        var updatedVersion = module.deleteFabricType(fabricTypeId, version, agent).block();

        module.removeFabricTypeFromLookup(fabricTypeId).block();
        module.removePermissionsForFabricType(fabricTypeId).block();

        return updatedVersion;
    }

    public List<FabricTypeDetails> getFabricTypes(Agent agent) {
        return getFabricTypes("", 0, Integer.MAX_VALUE, agent);
    }

    public List<FabricTypeDetails> getFabricTypes(String searchTerm, long skip, long limit, Agent agent) {
        return module.getFabricTypes(searchTerm, skip, limit, agent).block().getResults();
    }

}
