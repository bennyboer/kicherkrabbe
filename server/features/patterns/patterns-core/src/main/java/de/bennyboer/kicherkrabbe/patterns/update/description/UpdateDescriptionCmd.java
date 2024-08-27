package de.bennyboer.kicherkrabbe.patterns.update.description;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.patterns.PatternDescription;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateDescriptionCmd implements Command {

    @Nullable
    PatternDescription description;

    public static UpdateDescriptionCmd of(@Nullable PatternDescription description) {
        return new UpdateDescriptionCmd(description);
    }

    public Optional<PatternDescription> getDescription() {
        return Optional.ofNullable(description);
    }

}
