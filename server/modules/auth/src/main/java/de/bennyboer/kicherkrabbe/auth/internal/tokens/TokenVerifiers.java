package de.bennyboer.kicherkrabbe.auth.internal.tokens;

import de.bennyboer.kicherkrabbe.auth.internal.keys.KeyPair;

public class TokenVerifiers {

    public static TokenVerifier create(KeyPair keyPair) {
        return new JWTTokenVerifier(keyPair);
    }

}
