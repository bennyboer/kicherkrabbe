package de.bennyboer.kicherkrabbe.fabrics.themes.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.themes.ThemeName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateCmd implements Command {

    ThemeName name;

    public static UpdateCmd of(ThemeName name) {
        notNull(name, "Theme name must be given");

        return new UpdateCmd(name);
    }

}
