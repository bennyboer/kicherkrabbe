package de.bennyboer.kicherkrabbe.patterns;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PublishedPattern {

    PatternId id;

    PatternName name;

    PatternNumber number;

    @Nullable
    PatternDescription description;

    PatternAlias alias;

    PatternAttribution attribution;

    Set<PatternCategoryId> categories;

    List<ImageId> images;

    List<PatternVariant> variants;

    List<PatternExtra> extras;

    public static PublishedPattern of(
            PatternId id,
            PatternName name,
            PatternNumber number,
            @Nullable PatternDescription description,
            PatternAlias alias,
            PatternAttribution attribution,
            Set<PatternCategoryId> categories,
            List<ImageId> images,
            List<PatternVariant> variants,
            List<PatternExtra> extras
    ) {
        notNull(id, "Pattern ID must be given");
        notNull(name, "Pattern name must be given");
        notNull(number, "Pattern number must be given");
        notNull(alias, "Pattern alias must be given");
        notNull(attribution, "Pattern attribution must be given");
        notNull(categories, "Pattern categories must be given");
        notNull(images, "Pattern images must be given");
        notNull(variants, "Pattern variants must be given");
        notNull(extras, "Pattern extras must be given");

        return new PublishedPattern(
                id,
                name,
                number,
                description,
                alias,
                attribution,
                categories,
                images,
                variants,
                extras
        );
    }

    public Optional<PatternDescription> getDescription() {
        return Optional.ofNullable(description);
    }

}
