package de.bennyboer.kicherkrabbe.embroideries.delete.category;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RemoveCategoryCmd implements Command {

    EmbroideryCategoryId categoryId;

    public static RemoveCategoryCmd of(EmbroideryCategoryId categoryId) {
        notNull(categoryId, "Category ID to remove must be given");

        return new RemoveCategoryCmd(categoryId);
    }

}
