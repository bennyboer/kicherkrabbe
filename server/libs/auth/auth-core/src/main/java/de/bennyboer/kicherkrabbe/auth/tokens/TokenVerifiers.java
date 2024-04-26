package de.bennyboer.kicherkrabbe.auth.tokens;

import de.bennyboer.kicherkrabbe.auth.keys.KeyPair;

public class TokenVerifiers {

    public static TokenVerifier create(KeyPair keyPair) {
        return new JWTTokenVerifier(keyPair);
    }

}
