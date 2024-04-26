package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.CredentialsLookup;
import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.internal.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.internal.CredentialsService;
import de.bennyboer.kicherkrabbe.credentials.internal.Name;
import de.bennyboer.kicherkrabbe.credentials.internal.UserId;
import de.bennyboer.kicherkrabbe.credentials.internal.password.Password;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor
public class CredentialsModule {

    private final CredentialsService credentialsService;

    private final CredentialsLookupRepo credentialsLookupRepo;

    @Transactional
    public Mono<String> createCredentials(
            String name,
            String password,
            String userId
    ) {
        return credentialsService.create(
                Name.of(name),
                Password.of(password),
                UserId.of(userId),
                Agent.system()
        ).map(result -> result.getId().getValue());
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
                .then(Mono.fromCallable(() -> UseCredentialsResult.of("token"))); // TODO Use proper token generation
    }

    public Mono<Void> updateCredentialsInLookup(String credentialsId) {
        return credentialsService.get(CredentialsId.of(credentialsId))
                .flatMap(credentials -> credentialsLookupRepo.update(CredentialsLookup.of(
                        credentials.getId(),
                        credentials.getName()
                )));
    }

    private Mono<CredentialsId> findCredentialsByName(Name name) {
        return credentialsLookupRepo.findCredentialsIdByName(name);
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
