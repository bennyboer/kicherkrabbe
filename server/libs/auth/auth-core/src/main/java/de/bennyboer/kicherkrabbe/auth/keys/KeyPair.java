package de.bennyboer.kicherkrabbe.auth.keys;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyPair {

    ECPublicKey publicKey;

    ECPrivateKey privateKey;

    public static KeyPair of(ECPublicKey publicKey, ECPrivateKey privateKey) {
        notNull(publicKey, "Public key must be given");
        notNull(privateKey, "Private key must be given");

        return new KeyPair(publicKey, privateKey);
    }

}
