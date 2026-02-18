package de.bennyboer.kicherkrabbe.assets.persistence.lookup;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.ContentType;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupAsset implements VersionedReadModel<AssetId> {

    AssetId id;

    Version version;

    ContentType contentType;

    long fileSize;

    Instant createdAt;

    public static LookupAsset of(
            AssetId id,
            Version version,
            ContentType contentType,
            long fileSize,
            Instant createdAt
    ) {
        notNull(id, "Asset ID must be given");
        notNull(version, "Version must be given");
        notNull(contentType, "Content type must be given");
        check(fileSize >= 0, "File size must not be negative");
        notNull(createdAt, "Created at must be given");

        return new LookupAsset(id, version, contentType, fileSize, createdAt);
    }

}
