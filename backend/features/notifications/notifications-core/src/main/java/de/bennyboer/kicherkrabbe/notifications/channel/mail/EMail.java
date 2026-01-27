package de.bennyboer.kicherkrabbe.notifications.channel.mail;

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
public class EMail {

    String value;

    public static EMail of(String value) {
        notNull(value, "E-Mail must be given");
        check(!value.isBlank(), "E-Mail must not be blank");
        check(EmailValidator.getInstance().isValid(value), "E-Mail must be valid");

        return new EMail(value);
    }

    @Override
    public String toString() {
        return "EMail(%s)".formatted(value);
    }

}
