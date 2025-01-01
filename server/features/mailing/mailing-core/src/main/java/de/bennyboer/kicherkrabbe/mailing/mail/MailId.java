package de.bennyboer.kicherkrabbe.mailing.mail;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class MailId {

    String value;

    public static MailId of(String value) {
        notNull(value, "Mail ID must be given");
        check(!value.isBlank(), "Mail ID must not be blank");

        return new MailId(value);
    }

    public static MailId create() {
        return new MailId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "MailId(%s)".formatted(value);
    }

}
