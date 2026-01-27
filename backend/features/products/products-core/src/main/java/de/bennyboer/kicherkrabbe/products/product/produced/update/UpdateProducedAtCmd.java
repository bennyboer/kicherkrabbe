package de.bennyboer.kicherkrabbe.products.product.produced.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateProducedAtCmd implements Command {

    Instant producedAt;

    public static UpdateProducedAtCmd of(Instant producedAt) {
        notNull(producedAt, "Produced at date must be given");

        return new UpdateProducedAtCmd(producedAt);
    }

}
