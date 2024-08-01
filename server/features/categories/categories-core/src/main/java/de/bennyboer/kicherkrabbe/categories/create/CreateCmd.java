package de.bennyboer.kicherkrabbe.categories.create;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.categories.CategoryName;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreateCmd implements Command {

    CategoryName name;

    CategoryGroup group;

    public static CreateCmd of(CategoryName name, CategoryGroup group) {
        notNull(name, "Category name must be given");
        notNull(group, "Category group must be given");

        return new CreateCmd(name, group);
    }

}
