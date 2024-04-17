package de.bennyboer.kicherkrabbe.auth.internal.tokens;

import de.bennyboer.kicherkrabbe.auth.internal.keys.KeyPair;
import de.bennyboer.kicherkrabbe.auth.internal.keys.KeyPairs;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TokenGeneratorTests {

    @Test
    void shouldGenerateToken() {
        // given: a token generator and verifier
        KeyPair keyPair = KeyPairs.read("/keys/key_pair.pem");
        TokenGenerator tokenGenerator = TokenGenerators.create(keyPair);
        TokenVerifier tokenVerifier = TokenVerifiers.create(keyPair);

        // when: generating a token
        Owner owner = Owner.of(OwnerId.of("TEST_USER_ID"));
        Token token = tokenGenerator.generate(TokenPayload.of(owner)).block();

        // then: the token should not be null
        assertNotNull(token);

        // and: the token should contain the user ID
        TokenPayload payload = tokenVerifier.verify(token).block();
        assertThat(payload.getOwner().getId()).isEqualTo(OwnerId.of("TEST_USER_ID"));
    }

}
