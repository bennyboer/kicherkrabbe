package de.bennyboer.kicherkrabbe.auth.tokens;

import de.bennyboer.kicherkrabbe.auth.keys.KeyPair;
import de.bennyboer.kicherkrabbe.auth.keys.KeyPairs;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenGeneratorTests {

    @Test
    void shouldGenerateToken() {
        // given: a token generator and verifier
        KeyPair keyPair = KeyPairs.readFromClassPath("/keys/key_pair.pem");
        TokenGenerator tokenGenerator = TokenGenerators.create(keyPair);
        TokenVerifier tokenVerifier = TokenVerifiers.create(keyPair);

        // when: generating a token
        Owner owner = Owner.of(OwnerId.of("TEST_USER_ID"));
        Token token = tokenGenerator.generate(TokenPayload.of(owner)).block();

        // then: the token should not be null
        assertThat(token).isNotNull();

        // and: the token should contain the user ID
        TokenPayload payload = tokenVerifier.verify(token).block();
        assertThat(payload.getOwner().getId()).isEqualTo(OwnerId.of("TEST_USER_ID"));
    }

}
