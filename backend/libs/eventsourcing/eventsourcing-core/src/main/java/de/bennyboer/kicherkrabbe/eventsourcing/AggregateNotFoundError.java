package de.bennyboer.kicherkrabbe.eventsourcing;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import lombok.Getter;

@Getter
public class AggregateNotFoundError extends Exception {

    private final AggregateType type;

    private final AggregateId id;

    public AggregateNotFoundError(AggregateType type, AggregateId id) {
        super("Aggregate not found with type '%s' and ID '%s'".formatted(type.getValue(), id.getValue()));

        this.type = type;
        this.id = id;
    }

}
