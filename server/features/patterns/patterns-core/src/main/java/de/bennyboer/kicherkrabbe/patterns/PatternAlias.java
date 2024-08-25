package de.bennyboer.kicherkrabbe.patterns;

import com.github.slugify.Slugify;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternAlias {

    String value;

    public static PatternAlias of(String value) {
        notNull(value, "Pattern alias must be given");
        check(!value.isBlank(), "Pattern alias must not be blank");

        return new PatternAlias(value);
    }

    public static PatternAlias fromName(PatternName name) {
        notNull(name, "Pattern name must be given");

        var slugify = Slugify.builder()
                .locale(Locale.GERMAN)
                .build();

        return of(slugify.slugify(name.getValue()));
    }

    @Override
    public String toString() {
        return "PatternAlias(%s)".formatted(value);
    }

}
