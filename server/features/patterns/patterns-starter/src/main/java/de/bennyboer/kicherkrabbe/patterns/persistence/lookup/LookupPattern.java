package de.bennyboer.kicherkrabbe.patterns.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.patterns.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupPattern {

    PatternId id;

    Version version;

    boolean published;

    PatternName name;

    @Nullable // TODO Remove after all patterns have a number
    PatternNumber number;

    @Nullable
    PatternDescription description;

    PatternAlias alias;

    PatternAttribution attribution;

    Set<PatternCategoryId> categories;

    List<ImageId> images;

    List<PatternVariant> variants;

    List<PatternExtra> extras;

    Instant createdAt;

    public static LookupPattern of(
            PatternId id,
            Version version,
            boolean published,
            PatternName name,
            PatternNumber number,
            @Nullable PatternDescription description,
            PatternAlias alias,
            PatternAttribution attribution,
            Set<PatternCategoryId> categories,
            List<ImageId> images,
            List<PatternVariant> variants,
            List<PatternExtra> extras,
            Instant createdAt
    ) {
        notNull(id, "Pattern ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Name must be given");
        //        notNull(number, "Number must be given"); // TODO Enforce after all patterns have a number
        notNull(alias, "Alias must be given");
        notNull(attribution, "Attribution must be given");
        notNull(categories, "Categories must be given");
        notNull(images, "Images must be given");
        notNull(variants, "Variants must be given");
        notNull(extras, "Extras must be given");
        notNull(createdAt, "Creation date must be given");

        return new LookupPattern(
                id,
                version,
                published,
                name,
                number,
                description,
                alias,
                attribution,
                categories,
                images,
                variants,
                extras,
                createdAt
        );
    }

    public Optional<PatternDescription> getDescription() {
        return Optional.ofNullable(description);
    }

}
