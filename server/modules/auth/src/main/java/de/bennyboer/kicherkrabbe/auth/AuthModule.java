package de.bennyboer.kicherkrabbe.auth;

import de.bennyboer.kicherkrabbe.auth.internal.keys.KeyPair;
import de.bennyboer.kicherkrabbe.auth.internal.keys.KeyPairs;
import de.bennyboer.kicherkrabbe.auth.internal.tokens.TokenGenerator;
import de.bennyboer.kicherkrabbe.auth.internal.tokens.TokenGenerators;
import de.bennyboer.kicherkrabbe.auth.internal.tokens.TokenVerifier;
import de.bennyboer.kicherkrabbe.auth.internal.tokens.TokenVerifiers;
import de.bennyboer.kicherkrabbe.auth.ports.http.HttpConfig;
import de.bennyboer.kicherkrabbe.auth.ports.http.HttpHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Import({
        HttpConfig.class,
        HttpHandler.class
})
public class AuthModule {

    @Bean
    PasswordEncoder passwordEncoder() {
        return de.bennyboer.kicherkrabbe.auth.internal.credentials.password.PasswordEncoder.getInstance()
                .getInternalEncoder();
    }

    @Bean
    KeyPair keyPair() {
        return KeyPairs.read("/keys/key_pair.pem");
    }

    @Bean
    TokenVerifier tokenVerifier(KeyPair keyPair) {
        return TokenVerifiers.create(keyPair);
    }

    @Bean
    TokenGenerator tokenGenerator(KeyPair keyPair) {
        return TokenGenerators.create(keyPair);
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(
            ReactiveUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        var authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);

        return authenticationManager;
    }

}
