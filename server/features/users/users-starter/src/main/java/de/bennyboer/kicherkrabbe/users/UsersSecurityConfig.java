package de.bennyboer.kicherkrabbe.users;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Mono;

import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

@Configuration
public class UsersSecurityConfig {

    @Bean
    @Primary
    public ReactiveUserDetailsService usersReactiveUserDetailsService() {
        return username -> Mono.just(User
                .withUsername("default")
                .authorities(NO_AUTHORITIES)
                .build());
    }

}
