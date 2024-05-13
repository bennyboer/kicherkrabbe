package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.colors.persistence.lookup.ColorLookupRepo;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.inmemory.InMemoryColorLookupRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class ColorsModuleTest {

    private final ColorsModuleConfig config = new ColorsModuleConfig();

    private final ColorService colorService = new ColorService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher()
    );

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    private final ColorLookupRepo colorLookupRepo = new InMemoryColorLookupRepo();

    private final ColorsModule module = config.colorsModule(
            colorService,
            permissionsService,
            colorLookupRepo,
            agent -> Flux.empty()
    );

    public void allowUserToCreateColors(String userId) {
        module.allowUserToCreateColors(userId).block();
    }

    public String createColor(String name, int red, int green, int blue, Agent agent) {
        String colorId = module.createColor(name, red, green, blue, agent).block();

        module.updateColorInLookup(colorId).block();
        if (agent.getType() == AgentType.USER) {
            module.allowCreatorToManageColor(colorId, agent.getId().getValue()).block();
        }

        return colorId;
    }

    public long updateColor(String colorId, long version, String name, int red, int green, int blue, Agent agent) {
        var updatedVersion = module.updateColor(colorId, version, name, red, green, blue, agent).block();

        module.updateColorInLookup(colorId).block();

        return updatedVersion;
    }

    public long deleteColor(String colorId, long version, Agent agent) {
        var updatedVersion = module.deleteColor(colorId, version, agent).block();

        module.removeColorFromLookup(colorId).block();
        module.removePermissionsForColor(colorId).block();

        return updatedVersion;
    }

    public List<ColorDetails> getColors(Agent agent) {
        return getColors("", 0, Integer.MAX_VALUE, agent);
    }

    public List<ColorDetails> getColors(String searchTerm, long skip, long limit, Agent agent) {
        return module.getColors(searchTerm, skip, limit, agent).block().getResults();
    }

}
