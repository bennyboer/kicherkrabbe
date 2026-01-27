package de.bennyboer.kicherkrabbe.changes;

import reactor.core.publisher.Flux;

public interface ResourceChangesTracker {

    Flux<ResourceChange> getChanges(ReceiverId receiverId);

}
