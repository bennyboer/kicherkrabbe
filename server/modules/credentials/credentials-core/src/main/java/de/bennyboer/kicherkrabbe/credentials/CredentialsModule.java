package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.auth.tokens.*;
import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.CredentialsLookup;
import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.internal.*;
import de.bennyboer.kicherkrabbe.credentials.internal.create.NameAlreadyTakenError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor
public class CredentialsModule {

    private final CredentialsService credentialsService;

    private final CredentialsLookupRepo credentialsLookupRepo;

    private final TokenGenerator tokenGenerator;

    @Transactional
    public Mono<String> createCredentials(
            String name,
            String password,
            String userId
    ) {
        // TODO Pass agent and check permissions
        return assertNameNotAlreadyTaken(Name.of(name))
                .then(credentialsService.create(
                        Name.of(name),
                        Password.of(password),
                        UserId.of(userId),
                        Agent.system()
                ))
                .map(result -> result.getId().getValue());
    }

    @Transactional
    public Mono<UseCredentialsResult> useCredentials(String name, String password) {
        // TODO Pass agent and check permissions
        return tryToUseCredentialsAndReturnCredentials(Name.of(name), Password.of(password))
                .flatMap(credentials -> generateAccessTokenForCredentialsUser(credentials.getUserId()))
                .map(token -> UseCredentialsResult.of(token.getValue()));
    }

    @Transactional
    public Mono<Void> deleteCredentials(String credentialsId) {
        // TODO Pass agent and check permissions
        return credentialsService.delete(CredentialsId.of(credentialsId), Agent.system()).then();
    }

    @Transactional
    public Flux<String> deleteCredentialsByUserId(String userId) {
        // TODO check permissions
        return findCredentialsByUserId(UserId.of(userId))
                .delayUntil(credentialsId -> credentialsService.delete(credentialsId, Agent.system()))
                .map(CredentialsId::getValue);
    }

    public Mono<Void> updateCredentialsInLookup(String credentialsId) {
        return credentialsService.get(CredentialsId.of(credentialsId))
                .flatMap(credentials -> credentialsLookupRepo.update(CredentialsLookup.of(
                        credentials.getId(),
                        credentials.getName(),
                        credentials.getUserId()
                )));
    }

    public Mono<Void> removeCredentialsFromLookup(String credentialsId) {
        return credentialsLookupRepo.remove(CredentialsId.of(credentialsId));
    }

    private Mono<CredentialsId> findCredentialsByName(Name name) {
        return credentialsLookupRepo.findCredentialsIdByName(name);
    }

    private Flux<CredentialsId> findCredentialsByUserId(UserId userId) {
        return credentialsLookupRepo.findCredentialsIdByUserId(userId);
    }

    private Mono<Credentials> tryToUseCredentialsAndReturnCredentials(Name name, Password password) {
        return findCredentialsByName(name)
                .delayUntil(credentialsId -> credentialsService.use(
                        credentialsId,
                        name,
                        password,
                        Agent.anonymous()
                ))
                .flatMap(credentialsService::get);
    }

    private Mono<Token> generateAccessTokenForCredentialsUser(UserId userId) {
        var owner = Owner.of(OwnerId.of(userId.getValue()));
        var payload = TokenPayload.of(owner);

        return tokenGenerator.generate(payload);
    }

    private Mono<Void> assertNameNotAlreadyTaken(Name name) {
        return findCredentialsByName(name)
                .flatMap(credentialsId -> Mono.error(new NameAlreadyTakenError(name.getValue())));
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
