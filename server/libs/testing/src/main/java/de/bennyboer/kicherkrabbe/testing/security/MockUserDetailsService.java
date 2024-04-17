package de.bennyboer.kicherkrabbe.testing.security;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class MockUserDetailsService implements ReactiveUserDetailsService {

    private final Map<String, UserDetails> users = new HashMap<>();

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.fromSupplier(() -> users.get(username));
    }

    public void addUser(UserDetails user) {
        users.put(user.getUsername(), user);
    }

}
