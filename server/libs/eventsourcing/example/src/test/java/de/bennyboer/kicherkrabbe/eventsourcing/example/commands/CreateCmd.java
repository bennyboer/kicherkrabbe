package de.bennyboer.kicherkrabbe.eventsourcing.example.commands;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCmd implements Command {

    String title;

    String description;

    /**
     * Note that it does not really make sense to include this field in the command and created event.
     * It is just here to demonstrate a patch.
     */
    @Nullable
    Instant deletedAt;

    public static CreateCmd of(String title, String description, @Nullable Instant deletedAt) {
        notNull(title, "Title must be given");
        check(!title.isBlank(), "Title must not be blank");
        notNull(description, "Description must be given");
        check(!description.isBlank(), "Description must not be blank");

        return new CreateCmd(title, description, deletedAt);
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

}
