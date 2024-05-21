package de.bennyboer.kicherkrabbe.server;

import de.bennyboer.kicherkrabbe.auth.keys.KeyPair;
import de.bennyboer.kicherkrabbe.auth.keys.KeyPairs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class ServerSecurityConfig {

    @Bean
    KeyPair keyPair(@Value("${kicherkrabbe.key-pair:#{null}}") String keyPairPath) {
        return Optional.ofNullable(keyPairPath)
                .map(KeyPairs::readFromFile)
                .orElseGet(() -> KeyPairs.readFromClassPath("/keys/key_pair.pem"));
    }

}
