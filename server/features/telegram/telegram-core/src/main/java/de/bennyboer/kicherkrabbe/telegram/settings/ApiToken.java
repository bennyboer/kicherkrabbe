package de.bennyboer.kicherkrabbe.telegram.settings;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ApiToken {

    String value;

    public static ApiToken of(String value) {
        notNull(value, "API token must be given");
        check(!value.isBlank(), "API token must not be blank");

        return new ApiToken(value);
    }

    @Override
    public String toString() {
        return "ApiToken(%s)".formatted(value);
    }

}
