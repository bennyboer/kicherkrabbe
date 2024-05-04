package de.bennyboer.kicherkrabbe.fabrics.aggregate.update.themes;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.themes.ThemeId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateThemesCmd implements Command {

    Set<ThemeId> themes;

    public static UpdateThemesCmd of(Set<ThemeId> themes) {
        notNull(themes, "Themes must be given");

        return new UpdateThemesCmd(themes);
    }

}
