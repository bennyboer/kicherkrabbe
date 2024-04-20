package de.bennyboer.kicherkrabbe.auth;

import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsService;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.Name;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.UserId;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.password.Password;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor
public class AuthModule {

    private final CredentialsService credentialsService;

    @Transactional
    public Mono<Void> createCredentials(
            String name,
            String password,
            String userId
    ) {
        return credentialsService.create(
                Name.of(name),
                Password.of(password),
                UserId.of(userId),
                Agent.system()
        ).then();
    }

    @Transactional
    public Mono<UseCredentialsResult> useCredentials(String name, String password) {
        return findCredentialsByName(Name.of(name))
                .flatMap(credentialsId -> credentialsService.use(
                        credentialsId,
                        Name.of(name),
                        Password.of(password),
                        Agent.anonymous()
                ).then())
                .then(Mono.fromCallable(() -> UseCredentialsResult.of("token")));
    }

    private Mono<CredentialsId> findCredentialsByName(Name name) {
        return Mono.empty(); // TODO We need a read model repo here!
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
