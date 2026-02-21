package de.bennyboer.kicherkrabbe.offers.categories.remove;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RemoveCategoryCmd implements Command {

    OfferCategoryId categoryId;

    public static RemoveCategoryCmd of(OfferCategoryId categoryId) {
        notNull(categoryId, "Category ID must be given");

        return new RemoveCategoryCmd(categoryId);
    }

}
