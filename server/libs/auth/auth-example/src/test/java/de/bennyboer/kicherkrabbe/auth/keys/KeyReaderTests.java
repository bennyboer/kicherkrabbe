package de.bennyboer.kicherkrabbe.auth.keys;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyReaderTests {

    @Test
    void shouldReadKeyPairFromClassPath() {
        // given: a path to the key pair
        String path = "/keys/key_pair.pem";

        // when: reading the key pair
        KeyPair keyPair = KeyReader.readKeyPairFromClassPath(path).block();

        // then: the key pair should not be null
        assertThat(keyPair).isNotNull();

        // and: the public key should not be null
        assertThat(keyPair.getPublicKey()).isNotNull();

        // and: the private key should not be null
        assertThat(keyPair.getPrivateKey()).isNotNull();
    }

    @Test
    void shouldReadKeyPairFromFile() {
        // given: a path to the key pair
        String path = "src/test/resources/keys/key_pair.pem";

        // when: reading the key pair
        KeyPair keyPair = KeyReader.readKeyPairFromFile(path).block();

        // then: the key pair should not be null
        assertThat(keyPair).isNotNull();

        // and: the public key should not be null
        assertThat(keyPair.getPublicKey()).isNotNull();

        // and: the private key should not be null
        assertThat(keyPair.getPrivateKey()).isNotNull();
    }

}
