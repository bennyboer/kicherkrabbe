package de.bennyboer.kicherkrabbe.assets;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.assets.LocationType.FILE;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Location {

    LocationType type;

    @Nullable
    FileName fileName;

    public static Location file(FileName fileName) {
        notNull(fileName, "File path must be given");

        return new Location(FILE, fileName);
    }

    public Optional<FileName> getFileName() {
        return Optional.ofNullable(fileName);
    }

}

