package de.bennyboer.kicherkrabbe.offers.size.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.offers.OfferSize;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateSizeCmd implements Command {

    OfferSize size;

    public static UpdateSizeCmd of(OfferSize size) {
        notNull(size, "Size must be given");

        return new UpdateSizeCmd(size);
    }

}
