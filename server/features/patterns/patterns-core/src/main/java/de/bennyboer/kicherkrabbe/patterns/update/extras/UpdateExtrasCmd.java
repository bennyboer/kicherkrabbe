package de.bennyboer.kicherkrabbe.patterns.update.extras;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.patterns.PatternExtra;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateExtrasCmd implements Command {

    List<PatternExtra> extras;

    public static UpdateExtrasCmd of(List<PatternExtra> extras) {
        notNull(extras, "Extras must be given");
        check(!extras.isEmpty(), "Extras must not be empty");

        return new UpdateExtrasCmd(extras);
    }

}
