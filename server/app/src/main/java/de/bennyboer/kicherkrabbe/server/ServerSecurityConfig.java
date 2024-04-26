package de.bennyboer.kicherkrabbe.server;

import de.bennyboer.kicherkrabbe.auth.keys.KeyPair;
import de.bennyboer.kicherkrabbe.auth.keys.KeyPairs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerSecurityConfig {

    @Bean
    KeyPair keyPair() {
        return KeyPairs.read("/keys/key_pair.pem");
    }

}
