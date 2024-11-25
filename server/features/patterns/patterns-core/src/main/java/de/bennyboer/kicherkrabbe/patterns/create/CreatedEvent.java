package de.bennyboer.kicherkrabbe.patterns.create;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.patterns.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    PatternName name;

    PatternNumber number;

    @Nullable
    PatternDescription description;

    PatternAttribution attribution;

    Set<PatternCategoryId> categories;

    List<ImageId> images;

    List<PatternVariant> variants;

    List<PatternExtra> extras;

    public static CreatedEvent of(
            PatternName name,
            PatternNumber number,
            @Nullable PatternDescription description,
            PatternAttribution attribution,
            Set<PatternCategoryId> categories,
            List<ImageId> images,
            List<PatternVariant> variants,
            List<PatternExtra> extras
    ) {
        notNull(name, "Pattern name must be given");
        //        notNull(number, "Pattern number must be given"); // TODO Enable after all patterns have a number
        notNull(attribution, "Attribution must be given");
        notNull(categories, "Categories must be given");
        notNull(images, "Images must be given");
        notNull(variants, "Variants must be given");
        notNull(extras, "Extras must be given");
        check(!images.isEmpty(), "Images must not be empty");
        check(!variants.isEmpty(), "Variants must not be empty");

        return new CreatedEvent(
                name,
                number,
                description,
                attribution,
                categories,
                images,
                variants,
                extras
        );
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    public Optional<PatternDescription> getDescription() {
        return Optional.ofNullable(description);
    }

}
