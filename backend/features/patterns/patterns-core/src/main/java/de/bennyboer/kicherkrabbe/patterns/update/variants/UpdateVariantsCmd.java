package de.bennyboer.kicherkrabbe.patterns.update.variants;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.patterns.PatternVariant;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateVariantsCmd implements Command {

    List<PatternVariant> variants;

    public static UpdateVariantsCmd of(List<PatternVariant> variants) {
        notNull(variants, "Variants must be given");
        check(!variants.isEmpty(), "Variants must not be empty");

        return new UpdateVariantsCmd(variants);
    }

}
