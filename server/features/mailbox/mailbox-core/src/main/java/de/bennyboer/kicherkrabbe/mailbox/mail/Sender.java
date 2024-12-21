package de.bennyboer.kicherkrabbe.mailbox.mail;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Sender {

    SenderName name;

    EMail mail;

    @Nullable
    PhoneNumber phone;

    public static Sender of(SenderName name, EMail mail, @Nullable PhoneNumber phone) {
        notNull(name, "Sender name must be given");
        notNull(mail, "Sender mail must be given");

        return new Sender(name, mail, phone);
    }

    public Optional<PhoneNumber> getPhone() {
        return Optional.ofNullable(phone);
    }

    public Sender anonymize() {
        return withName(getName().anonymize())
                .withMail(getMail().anonymize())
                .withPhone(null);
    }

    @Override
    public String toString() {
        return "Sender(name=%s, mail=%s, phone=%s)".formatted(name, mail, phone);
    }

}
