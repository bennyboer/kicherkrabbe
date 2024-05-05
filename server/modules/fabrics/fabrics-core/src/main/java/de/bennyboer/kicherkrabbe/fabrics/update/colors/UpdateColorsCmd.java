package de.bennyboer.kicherkrabbe.fabrics.update.colors;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateColorsCmd implements Command {

    Set<ColorId> colors;

    public static UpdateColorsCmd of(Set<ColorId> colors) {
        notNull(colors, "Colors must be given");

        return new UpdateColorsCmd(colors);
    }

}
