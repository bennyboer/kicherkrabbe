package de.bennyboer.kicherkrabbe.users;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.apache.commons.validator.routines.EmailValidator;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Mail {

    private static final String ANONYMIZED = "anonymized@kicherkrabbe.com";

    String value;

    public static Mail of(String value) {
        notNull(value, "Mail must be given");
        check(EmailValidator.getInstance().isValid(value), "Mail must be valid");

        return new Mail(value);
    }

    public Mail anonymize() {
        return withValue(ANONYMIZED);
    }

    @Override
    public String toString() {
        return "Mail(%s)".formatted(value);
    }

}
