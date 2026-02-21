package de.bennyboer.kicherkrabbe.offers.title.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.offers.OfferTitle;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateTitleCmd implements Command {

    OfferTitle title;

    public static UpdateTitleCmd of(OfferTitle title) {
        notNull(title, "Title must be given");

        return new UpdateTitleCmd(title);
    }

}
