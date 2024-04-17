package de.bennyboer.kicherkrabbe.testing.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;

@Configuration
public class SecurityTestConfig {

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return new MockUserDetailsService();
    }

}
