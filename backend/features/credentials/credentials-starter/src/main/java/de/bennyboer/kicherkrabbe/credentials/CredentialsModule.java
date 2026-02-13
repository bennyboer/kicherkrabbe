package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.auth.tokens.*;
import de.bennyboer.kicherkrabbe.credentials.create.NameAlreadyTakenError;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.LookupCredentials;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.credentials.Actions.*;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

public class CredentialsModule {

    private final CredentialsService credentialsService;

    private final CredentialsLookupRepo credentialsLookupRepo;

    private final PermissionsService permissionsService;

    private final TokenGenerator tokenGenerator;

    private final RefreshTokenService refreshTokenService;

    private final ReactiveTransactionManager transactionManager;

    private boolean isInitialized;

    public CredentialsModule(
            CredentialsService credentialsService,
            CredentialsLookupRepo credentialsLookupRepo,
            PermissionsService permissionsService,
            TokenGenerator tokenGenerator,
            RefreshTokenService refreshTokenService,
            ReactiveTransactionManager transactionManager
    ) {
        this.credentialsService = credentialsService;
        this.credentialsLookupRepo = credentialsLookupRepo;
        this.permissionsService = permissionsService;
        this.tokenGenerator = tokenGenerator;
        this.refreshTokenService = refreshTokenService;
        this.transactionManager = transactionManager;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent ignoredEvent) {
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        initialize()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public Mono<Void> initialize() {
        TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);

        return createGroupPermissions()
                .as(transactionalOperator::transactional);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<String> createCredentials(
            String name,
            String password,
            String userId,
            Agent agent
    ) {
        return assertAgentIsAllowedTo(agent, CREATE)
                .then(assertNameNotAlreadyTaken(Name.of(name)))
                .then(credentialsService.create(
                        Name.of(name),
                        Password.of(password),
                        UserId.of(userId),
                        Agent.system()
                ))
                .map(result -> result.getId().getValue());
    }

    @Transactional(propagation = MANDATORY)
    public Mono<UseCredentialsResult> useCredentials(String name, String password, Agent agent) {
        return tryToUseCredentialsAndReturnCredentials(Name.of(name), Password.of(password), agent)
                .flatMap(credentials -> {
                    String userId = credentials.getUserId().getValue();
                    return generateAccessTokenForCredentialsUser(credentials.getUserId())
                            .flatMap(token -> refreshTokenService.generate(userId)
                                    .map(refreshToken -> UseCredentialsResult.of(
                                            token.getValue(),
                                            refreshToken.getTokenValue()
                                    )));
                });
    }

    public Mono<RefreshResult> refreshTokens(String refreshToken) {
        return refreshTokenService.refresh(refreshToken, tokenGenerator);
    }

    public Mono<Void> logout(String refreshToken) {
        return refreshTokenService.revokeFamily(refreshToken);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Void> deleteCredentials(String credentialsId, long version, Agent agent) {
        var id = CredentialsId.of(credentialsId);

        return assertAgentIsAllowedTo(agent, DELETE, id)
                .then(credentialsService.delete(CredentialsId.of(credentialsId), Version.of(version), Agent.system()))
                .then();
    }

    @Transactional(propagation = MANDATORY)
    public Flux<String> deleteCredentialsByUserId(String userId, Agent agent) {
        return refreshTokenService.revokeByUserId(userId)
                .thenMany(findCredentialsIdByUserId(UserId.of(userId))
                        .delayUntil(credentialsId -> assertAgentIsAllowedTo(agent, DELETE, credentialsId)
                                .then(credentialsService.delete(credentialsId, Agent.system())))
                        .map(CredentialsId::getValue));
    }

    public Mono<Void> updateCredentialsInLookup(String credentialsId) {
        return credentialsService.getOrThrow(CredentialsId.of(credentialsId))
                .flatMap(credentials -> credentialsLookupRepo.update(LookupCredentials.of(
                        credentials.getId(),
                        credentials.getVersion(),
                        credentials.getName(),
                        credentials.getUserId()
                )));
    }

    public Mono<Void> removeCredentialsFromLookup(String credentialsId) {
        return credentialsLookupRepo.remove(CredentialsId.of(credentialsId));
    }

    public Mono<Void> addPermissions(String credentialsId, String userId) {
        var resourceType = ResourceType.of("CREDENTIALS");
        var credentialsResource = Resource.of(resourceType, ResourceId.of(credentialsId));
        var user = Holder.user(HolderId.of(userId));

        var deleteCredentialsAsSystem = Permission.builder()
                .holder(Holder.group(HolderId.system()))
                .isAllowedTo(DELETE)
                .on(credentialsResource);

        var useCredentialsAsAnonymous = Permission.builder()
                .holder(Holder.group(HolderId.anonymous()))
                .isAllowedTo(USE)
                .on(credentialsResource);
        var useCredentialsAsUser = Permission.builder()
                .holder(user)
                .isAllowedTo(USE)
                .on(credentialsResource);

        return permissionsService.addPermissions(
                deleteCredentialsAsSystem,
                useCredentialsAsAnonymous,
                useCredentialsAsUser
        );
    }

    public Mono<Void> removePermissionsOnCredentials(String credentialsId) {
        var resourceType = ResourceType.of("CREDENTIALS");
        var credentialsResource = Resource.of(resourceType, ResourceId.of(credentialsId));

        return permissionsService.removePermissionsByResource(credentialsResource);
    }

    private Mono<CredentialsId> findCredentialsIdByName(Name name) {
        return credentialsLookupRepo.findCredentialsByName(name)
                .map(LookupCredentials::getId);
    }

    private Flux<CredentialsId> findCredentialsIdByUserId(UserId userId) {
        return credentialsLookupRepo.findCredentialsByUserId(userId)
                .map(LookupCredentials::getId);
    }

    private Mono<Credentials> tryToUseCredentialsAndReturnCredentials(Name name, Password password, Agent agent) {
        return findCredentialsIdByName(name)
                .delayUntil(credentialsId -> assertAgentIsAllowedTo(agent, USE, credentialsId)
                        .then(credentialsService.use(
                                credentialsId,
                                name,
                                password,
                                Agent.anonymous()
                        )))
                .flatMap(credentialsService::getOrThrow);
    }

    private Mono<Token> generateAccessTokenForCredentialsUser(UserId userId) {
        var owner = Owner.of(OwnerId.of(userId.getValue()));
        var payload = TokenPayload.of(owner);

        return tokenGenerator.generate(payload);
    }

    private Mono<Void> assertNameNotAlreadyTaken(Name name) {
        return findCredentialsIdByName(name)
                .flatMap(credentialsId -> Mono.error(new NameAlreadyTakenError(name.getValue())));
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable CredentialsId credentialsId) {
        Permission permission = toPermission(agent, action, credentialsId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable CredentialsId credentialsId) {
        Holder holder = toHolder(agent);
        var resourceType = ResourceType.of("CREDENTIALS");

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(credentialsId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(credentialsId.getValue()))))
                .orElseGet(() -> permissionBuilder.onType(resourceType));
    }

    private Holder toHolder(Agent agent) {
        if (agent.isSystem()) {
            return Holder.group(HolderId.system());
        } else if (agent.isAnonymous()) {
            return Holder.group(HolderId.anonymous());
        } else {
            return Holder.user(HolderId.of(agent.getId().getValue()));
        }
    }

    private Mono<Void> createGroupPermissions() {
        var credentialsType = ResourceType.of("CREDENTIALS");

        var createCredentialsAsSystem = Permission.builder()
                .holder(Holder.group(HolderId.system()))
                .isAllowedTo(CREATE)
                .onType(credentialsType);

        return permissionsService.addPermissions(createCredentialsAsSystem);
    }

    @Value
    @AllArgsConstructor(access = PRIVATE)
    public static class UseCredentialsResult {

        String token;

        String refreshToken;

        public static UseCredentialsResult of(String token, String refreshToken) {
            notNull(token, "Token must be given");
            check(!token.isBlank(), "Token must not be empty");
            notNull(refreshToken, "Refresh token must be given");
            check(!refreshToken.isBlank(), "Refresh token must not be empty");

            return new UseCredentialsResult(token, refreshToken);
        }

    }

}
