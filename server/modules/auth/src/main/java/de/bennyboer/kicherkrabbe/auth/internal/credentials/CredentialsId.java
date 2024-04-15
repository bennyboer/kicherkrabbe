package de.bennyboer.kicherkrabbe.auth.internal.credentials;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CredentialsId {

    String value;

    public static CredentialsId of(String value) {
        notNull(value, "CredentialsId must be given");
        check(!value.isBlank(), "CredentialsId must not be blank");

        return new CredentialsId(value);
    }

    public static CredentialsId create() {
        return of(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return "CredentialsId(%s)".formatted(value);
    }

}

