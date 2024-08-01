package de.bennyboer.kicherkrabbe.categories.rename;

import de.bennyboer.kicherkrabbe.categories.CategoryName;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RenameCmd implements Command {

    CategoryName name;

    public static RenameCmd of(CategoryName name) {
        notNull(name, "Category name must be given");

        return new RenameCmd(name);
    }

}
