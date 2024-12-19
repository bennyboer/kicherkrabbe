package de.bennyboer.kicherkrabbe.inquiries;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.commons.validator.routines.EmailValidator;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EMail {

    String value;

    public static EMail of(String value) {
        notNull(value, "E-Mail must be given");
        check(!value.isBlank(), "E-Mail must not be blank");
        check(EmailValidator.getInstance().isValid(value), "E-Mail must be valid");
        check(value.length() <= 70, "E-Mail must not exceed 70 characters");

        return new EMail(value);
    }

    @Override
    public String toString() {
        return "EMail(%s)".formatted(value);
    }

    public EMail anonymize() {
        return EMail.of("anonymized@kicherkrabbe.com");
    }

}
