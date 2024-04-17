package de.bennyboer.kicherkrabbe.auth;

import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsService;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor
public class AuthModule {

    private final CredentialsService credentialsService;

    public Mono<UseCredentialsResult> useCredentials(String name, String password) {
        return Mono.empty(); // TODO Test + implement
    }

    @Value
    @AllArgsConstructor(access = PRIVATE)
    public static class UseCredentialsResult {

        String token;

        public static UseCredentialsResult of(String token) {
            notNull(token, "Token must be given");
            check(!token.isBlank(), "Token must not be empty");

            return new UseCredentialsResult(token);
        }

    }

}
