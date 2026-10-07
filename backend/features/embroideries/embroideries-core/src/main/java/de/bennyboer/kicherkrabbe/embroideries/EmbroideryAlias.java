package de.bennyboer.kicherkrabbe.embroideries;

import com.github.slugify.Slugify;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EmbroideryAlias {

    private static final Slugify SLUGIFY = Slugify.builder()
            .locale(Locale.GERMAN)
            .build();

    String value;

    public static EmbroideryAlias of(String value) {
        notNull(value, "Embroidery alias must be given");
        check(!value.isBlank(), "Embroidery alias must not be blank");

        return new EmbroideryAlias(value);
    }

    public static EmbroideryAlias fromName(EmbroideryName name) {
        notNull(name, "Embroidery name must be given");

        return of(SLUGIFY.slugify(name.getValue()));
    }

    @Override
    public String toString() {
        return "EmbroideryAlias(%s)".formatted(value);
    }

}
