package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AssetDetails {

    AssetId id;

    Version version;

    ContentType contentType;

    long fileSize;

    Instant createdAt;

    List<AssetReference> references;

    public static AssetDetails of(
            AssetId id,
            Version version,
            ContentType contentType,
            long fileSize,
            Instant createdAt,
            List<AssetReference> references
    ) {
        notNull(id, "Asset ID must be given");
        notNull(version, "Version must be given");
        notNull(contentType, "Content type must be given");
        check(fileSize >= 0, "File size must not be negative");
        notNull(createdAt, "Created at must be given");
        notNull(references, "References must be given");

        return new AssetDetails(id, version, contentType, fileSize, createdAt, references);
    }

}
