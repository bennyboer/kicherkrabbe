package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import reactor.core.publisher.Flux;

public interface AccessibleColorsTracker {

    Flux<String> trackChanges(Agent agent);

}
