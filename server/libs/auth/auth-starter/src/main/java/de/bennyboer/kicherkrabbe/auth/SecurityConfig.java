package de.bennyboer.kicherkrabbe.auth;

import de.bennyboer.kicherkrabbe.auth.keys.KeyPair;
import de.bennyboer.kicherkrabbe.auth.keys.KeyPairs;
import de.bennyboer.kicherkrabbe.auth.testing.MockUserDetailsService;
import de.bennyboer.kicherkrabbe.auth.tokens.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    @ConditionalOnMissingBean(KeyPair.class)
    KeyPair keyPair() {
        return KeyPairs.generate();
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
    PasswordEncoder passwordEncoder() {
        return de.bennyboer.kicherkrabbe.auth.password.PasswordEncoder.getInstance().getInternalEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveUserDetailsService.class)
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return new MockUserDetailsService();
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

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveAuthenticationManager reactiveAuthenticationManager,
            TokenVerifier verifier,
            List<Customizer<ServerHttpSecurity.AuthorizeExchangeSpec>> authorizeExchangeCustomizers
    ) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authenticationManager(reactiveAuthenticationManager)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .addFilterAt(new JwtTokenAuthenticationFilter(verifier), SecurityWebFiltersOrder.HTTP_BASIC)
                .authorizeExchange(exchanges -> authorizeExchangeCustomizers.stream()
                        .reduce(exchanges, (acc, customizer) -> {
                            customizer.customize(acc);
                            return acc;
                        }, (acc1, acc2) -> acc1)
                        .anyExchange().authenticated());

        return http.build();
    }

}
