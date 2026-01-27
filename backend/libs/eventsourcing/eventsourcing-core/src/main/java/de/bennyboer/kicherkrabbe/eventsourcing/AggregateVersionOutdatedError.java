package de.bennyboer.kicherkrabbe.eventsourcing;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import lombok.Getter;

@Getter
public class AggregateVersionOutdatedError extends Exception {

    private final AggregateType type;

    private final AggregateId id;

    private final Version version;

    public AggregateVersionOutdatedError(AggregateType type, AggregateId id, Version version) {
        super("Aggregate version '%d' with type '%s' and ID '%s' is outdated".formatted(
                version.getValue(),
                type.getValue(),
                id.getValue()
        ));

        this.type = type;
        this.id = id;
        this.version = version;
    }

}
