package de.bennyboer.kicherkrabbe.fabrics.delete.colors;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RemoveColorCmd implements Command {

    ColorId colorId;

    public static RemoveColorCmd of(ColorId colorId) {
        notNull(colorId, "Color ID to remove must be given");

        return new RemoveColorCmd(colorId);
    }

}

