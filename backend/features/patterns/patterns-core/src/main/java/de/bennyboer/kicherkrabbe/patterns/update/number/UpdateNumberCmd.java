package de.bennyboer.kicherkrabbe.patterns.update.number;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.patterns.PatternNumber;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateNumberCmd implements Command {

    @Nullable
    PatternNumber number;

    public static UpdateNumberCmd of(PatternNumber number) {
        notNull(number, "Pattern number must be given");

        return new UpdateNumberCmd(number);
    }

}
