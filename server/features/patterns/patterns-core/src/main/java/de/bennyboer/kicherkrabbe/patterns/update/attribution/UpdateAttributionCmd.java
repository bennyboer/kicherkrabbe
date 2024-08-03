package de.bennyboer.kicherkrabbe.patterns.update.attribution;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.patterns.PatternAttribution;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateAttributionCmd implements Command {

    PatternAttribution attribution;

    public static UpdateAttributionCmd of(PatternAttribution attribution) {
        notNull(attribution, "Pattern attribution must be given");

        return new UpdateAttributionCmd(attribution);
    }

}
