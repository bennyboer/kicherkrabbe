package de.bennyboer.kicherkrabbe.patterns.delete.category;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RemoveCategoryCmd implements Command {

    PatternCategoryId categoryId;

    public static RemoveCategoryCmd of(PatternCategoryId categoryId) {
        notNull(categoryId, "Category ID to remove must be given");

        return new RemoveCategoryCmd(categoryId);
    }

}
