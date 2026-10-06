package de.bennyboer.kicherkrabbe.embroideries.update.categories;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateCategoriesCmd implements Command {

    Set<EmbroideryCategoryId> categories;

    public static UpdateCategoriesCmd of(Set<EmbroideryCategoryId> categories) {
        notNull(categories, "Categories must be given");

        return new UpdateCategoriesCmd(categories);
    }

}
