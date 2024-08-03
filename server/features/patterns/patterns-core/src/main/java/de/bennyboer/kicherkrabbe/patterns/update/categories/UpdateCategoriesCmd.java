package de.bennyboer.kicherkrabbe.patterns.update.categories;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateCategoriesCmd implements Command {

    Set<PatternCategoryId> categories;

    public static UpdateCategoriesCmd of(Set<PatternCategoryId> categories) {
        notNull(categories, "Categories must be given");
        check(!categories.isEmpty(), "Categories must not be empty");

        return new UpdateCategoriesCmd(categories);
    }

}
