package de.bennyboer.kicherkrabbe.categories.regroup;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RegroupCmd implements Command {

    CategoryGroup group;

    public static RegroupCmd of(CategoryGroup group) {
        notNull(group, "Category group must be given");

        return new RegroupCmd(group);
    }

}
