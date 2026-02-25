package de.bennyboer.kicherkrabbe.offers;

import com.github.slugify.Slugify;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class OfferAlias {

    private static final Slugify SLUGIFY = Slugify.builder()
            .locale(Locale.GERMAN)
            .build();

    String value;

    public static OfferAlias of(String value) {
        notNull(value, "Offer alias must be given");
        check(!value.isBlank(), "Offer alias must not be blank");

        return new OfferAlias(value);
    }

    public static OfferAlias fromTitle(OfferTitle title) {
        notNull(title, "Offer title must be given");

        return of(SLUGIFY.slugify(title.getValue()));
    }

    @Override
    public String toString() {
        return "OfferAlias(%s)".formatted(value);
    }

}
