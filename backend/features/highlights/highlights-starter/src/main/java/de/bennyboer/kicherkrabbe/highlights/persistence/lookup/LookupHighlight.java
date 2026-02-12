package de.bennyboer.kicherkrabbe.highlights.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import de.bennyboer.kicherkrabbe.highlights.HighlightId;
import de.bennyboer.kicherkrabbe.highlights.ImageId;
import de.bennyboer.kicherkrabbe.highlights.Links;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupHighlight implements VersionedReadModel<HighlightId> {

    HighlightId id;

    Version version;

    ImageId imageId;

    Links links;

    boolean published;

    long sortOrder;

    Instant createdAt;

    public static LookupHighlight of(
            HighlightId id,
            Version version,
            ImageId imageId,
            Links links,
            boolean published,
            long sortOrder,
            Instant createdAt
    ) {
        notNull(id, "Highlight ID must be given");
        notNull(version, "Version must be given");
        notNull(imageId, "Image ID must be given");
        notNull(links, "Links must be given");
        notNull(createdAt, "Creation date must be given");

        return new LookupHighlight(id, version, imageId, links, published, sortOrder, createdAt);
    }

}
