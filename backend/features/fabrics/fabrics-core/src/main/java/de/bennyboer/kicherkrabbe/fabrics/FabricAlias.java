package de.bennyboer.kicherkrabbe.fabrics;

import com.github.slugify.Slugify;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricAlias {

    private static final Slugify SLUGIFY = Slugify.builder()
            .locale(Locale.GERMAN)
            .build();

    String value;

    public static FabricAlias of(String value) {
        notNull(value, "Fabric alias must be given");
        check(!value.isBlank(), "Fabric alias must not be blank");

        return new FabricAlias(value);
    }

    public static FabricAlias fromName(FabricName name) {
        notNull(name, "Fabric name must be given");

        return of(SLUGIFY.slugify(name.getValue()));
    }

    @Override
    public String toString() {
        return "FabricAlias(%s)".formatted(value);
    }

}
