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
    FilePath filePath;

    public static Location file(FilePath filePath) {
        notNull(filePath, "File path must be given");

        return new Location(FILE, filePath);
    }

    public Optional<FilePath> getFilePath() {
        return Optional.ofNullable(filePath);
    }

}

