package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.UserLookup;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.UserLookupRepo;
import de.bennyboer.kicherkrabbe.users.create.MailAlreadyInUseError;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.users.Actions.CREATE;

@AllArgsConstructor
public class UsersModule {

    private final UsersService usersService;

    private final UserLookupRepo userLookupRepo;

    private final PermissionsService permissionsService;

    private final ReactiveTransactionManager transactionManager;

    @PostConstruct
    public void init() {
        initialize()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public Mono<Void> initialize() {
        return createGroupPermissions()
                .then(createDefaultUserIfNoneExists());
    }

    @Transactional
    public Mono<String> createUser(String firstName, String lastName, String mail, Agent agent) {
        var name = FullName.of(
                FirstName.of(firstName),
                LastName.of(lastName)
        );

        return assertAgentIsAllowedTo(agent, CREATE)
                .then(assertThatMailNotAlreadyInUse(Mail.of(mail)))
                .then(usersService.create(
                        name,
                        Mail.of(mail),
                        Agent.system()
                ))
                .map(result -> result.getId().getValue());
    }

    @Transactional
    public Mono<Void> deleteUser(String userId) {
        // TODO Check permissions
        return usersService.delete(UserId.of(userId), Agent.system()).then();
    }

    @Transactional
    public Mono<Void> renameUser(String userId, String firstName, String lastName) {
        var name = FullName.of(
                FirstName.of(firstName),
                LastName.of(lastName)
        );

        // TODO Check permissions
        return usersService.rename(
                UserId.of(userId),
                name,
                Agent.system()
        ).then();
    }

    public Mono<UserDetails> getUserDetails(String userId) {
        // TODO Check permissions
        return usersService.get(UserId.of(userId))
                .map(user -> UserDetails.of(user.getId(), user.getName(), user.getMail()));
    }

    public Mono<Void> updateUserInLookup(String userId) {
        return usersService.get(UserId.of(userId))
                .map(user -> UserLookup.of(user.getId(), user.getName(), user.getMail()))
                .flatMap(userLookupRepo::update);
    }

    public Mono<Void> removeUserFromLookup(String userId) {
        return userLookupRepo.remove(UserId.of(userId));
    }

    private Mono<Void> assertThatMailNotAlreadyInUse(Mail mail) {
        return userLookupRepo.findByMail(mail)
                .flatMap(user -> Mono.error(new MailAlreadyInUseError(mail.getValue())));
    }

    private Mono<Void> createDefaultUserIfNoneExists() {
        TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);

        // TODO Configure default user via configuration file
        return userLookupRepo.count()
                .filter(count -> count == 0)
                .flatMap(count -> createUser("Default", "User", "default@kicherkrabbe.com", Agent.system()))
                .as(transactionalOperator::transactional)
                .then();
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable UserId userId) {
        Permission permission = toPermission(agent, action, userId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable UserId userId) {
        Holder holder = toHolder(agent);
        var resourceType = ResourceType.of("USER");

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(userId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(userId.getValue()))))
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
        Permission createUsersAsSystemGroup = Permission.builder()
                .holder(Holder.group(HolderId.system()))
                .isAllowedTo(CREATE)
                .onType(ResourceType.of("USER"));

        return permissionsService.addPermissions(createUsersAsSystemGroup);
    }

}