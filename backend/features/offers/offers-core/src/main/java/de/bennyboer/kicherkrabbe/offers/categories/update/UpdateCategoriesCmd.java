package de.bennyboer.kicherkrabbe.offers.categories.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateCategoriesCmd implements Command {

    Set<OfferCategoryId> categories;

    public static UpdateCategoriesCmd of(Set<OfferCategoryId> categories) {
        notNull(categories, "Categories must be given");

        return new UpdateCategoriesCmd(categories);
    }

}
