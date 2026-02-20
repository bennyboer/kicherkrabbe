package de.bennyboer.kicherkrabbe.offers;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Notes {

    Note description;

    @Nullable
    Note contains;

    @Nullable
    Note care;

    @Nullable
    Note safety;

    public static Notes of(Note description, @Nullable Note contains, @Nullable Note care, @Nullable Note safety) {
        notNull(description, "Description note must be given");

        return new Notes(description, contains, care, safety);
    }

    public Optional<Note> getContains() {
        return Optional.ofNullable(contains);
    }

    public Optional<Note> getCare() {
        return Optional.ofNullable(care);
    }

    public Optional<Note> getSafety() {
        return Optional.ofNullable(safety);
    }

}
