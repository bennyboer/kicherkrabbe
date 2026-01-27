package de.bennyboer.kicherkrabbe.changes;

import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.HandleableEvent;

import java.util.Map;

public interface EventResourceChangePayloadTransformer {

    Map<String, Object> toChangePayload(HandleableEvent event);

}
