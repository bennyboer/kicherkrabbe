package de.bennyboer.kicherkrabbe.fabrics.aggregate;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;

public class FabricServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final FabricService fabricService = new FabricService(repo, eventPublisher);

    // TODO Test create, rename, publish, unpublish, delete, updateColors, updateImage, updateThemes,
    //  updateAvailability, snapshotting, ...

}
