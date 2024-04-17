package de.bennyboer.kicherkrabbe.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Mono;

import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        // TODO Move to users module when available
        // TODO Load users from database instead
        return username -> Mono.just(User
                .withUsername("default")
                .authorities(NO_AUTHORITIES)
                .build());
    }

}
