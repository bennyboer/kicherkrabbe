package de.bennyboer.kicherkrabbe.auth.tokens;

import de.bennyboer.kicherkrabbe.auth.keys.KeyPair;

public class TokenGenerators {

    public static TokenGenerator create(KeyPair keyPair) {
        return new JWTTokenGenerator(keyPair);
    }

}
