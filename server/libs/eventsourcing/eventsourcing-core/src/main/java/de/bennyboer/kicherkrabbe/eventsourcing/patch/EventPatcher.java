package de.bennyboer.kicherkrabbe.eventsourcing.patch;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.*;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EventPatcher {

    Map<AggregateType, Map<EventName, List<Patch>>> sortedPatchesLookup;

    public static EventPatcher fromPatches(List<Patch> patches) {
        Map<AggregateType, Map<EventName, List<Patch>>> sortedPatchesLookup = new HashMap<>();

        for (var patch : patches) {
            Map<EventName, List<Patch>> patchesForAggregateType = sortedPatchesLookup.computeIfAbsent(
                    patch.aggregateType(),
                    aggregateType -> new HashMap<>()
            );

            List<Patch> patchesForEventName = patchesForAggregateType.computeIfAbsent(
                    patch.eventName(),
                    eventName -> new ArrayList<>()
            );

            patchesForEventName.add(patch);
        }

        for (var patchesForAggregateType : sortedPatchesLookup.values()) {
            for (var patchesForEventName : patchesForAggregateType.values()) {
                patchesForEventName.sort(Comparator.comparing(Patch::fromVersion));
            }
        }

        return new EventPatcher(sortedPatchesLookup);
    }

    public Event patch(Event event, EventMetadata metadata) {
        var patchesForAggregateType = sortedPatchesLookup.get(metadata.getAggregateType());
        if (patchesForAggregateType == null) {
            return event;
        }

        var patchesForEventName = patchesForAggregateType.get(event.getEventName());
        if (patchesForEventName == null) {
            return event;
        }

        for (var patch : patchesForEventName) {
            if (patch.fromVersion().equals(event.getVersion())) {
                event = patch.apply(event);
            }
        }

        return event;
    }

}
