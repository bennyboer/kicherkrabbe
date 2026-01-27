package de.bennyboer.kicherkrabbe.eventsourcing.aggregate;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AggregateIdAndVersion<ID> {

    ID id;

    Version version;

    public static <ID> AggregateIdAndVersion<ID> of(ID id, Version version) {
        notNull(id, "Id must be given");
        notNull(version, "Version must be given");

        return new AggregateIdAndVersion<>(id, version);
    }

}
