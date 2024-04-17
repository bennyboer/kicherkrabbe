package de.bennyboer.kicherkrabbe.auth.internal.tokens;

import de.bennyboer.kicherkrabbe.auth.internal.keys.KeyPair;

public class TokenGenerators {

    public static TokenGenerator create(KeyPair keyPair) {
        return new JWTTokenGenerator(keyPair);
    }

}
