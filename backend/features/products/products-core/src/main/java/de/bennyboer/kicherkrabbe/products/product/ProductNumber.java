package de.bennyboer.kicherkrabbe.products.product;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.regex.Pattern;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ProductNumber {

    private static final Pattern YEARLY_FORMAT = Pattern.compile("^\\d{4}-\\d+$");
    private static final Pattern LEGACY_FORMAT = Pattern.compile("^\\d{5}$");

    String value;

    public static ProductNumber of(String value) {
        notNull(value, "Product number must be given");
        check(!value.isBlank(), "Product number must not be blank");
        check(
                YEARLY_FORMAT.matcher(value).matches() || LEGACY_FORMAT.matcher(value).matches(),
                "Product number must be in YYYY-N format or legacy 5-digit format"
        );

        return new ProductNumber(value);
    }

    @Override
    public String toString() {
        return "ProductNumber(%s)".formatted(value);
    }

}
