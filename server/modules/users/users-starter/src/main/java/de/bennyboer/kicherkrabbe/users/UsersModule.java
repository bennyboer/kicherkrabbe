package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.users.create.MailAlreadyInUseError;
import de.bennyboer.kicherkrabbe.users.persistence.lookup.LookupUser;
import de.bennyboer.kicherkrabbe.users.persistence.lookup.UserLookupRepo;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.users.Actions.*;

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
        TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);

        return createGroupPermissions()
                .then(createDefaultUserIfNoneExists())
                .as(transactionalOperator::transactional);
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
    public Mono<Void> deleteUser(String userId, long version, Agent agent) {
        var id = UserId.of(userId);

        return assertAgentIsAllowedTo(agent, DELETE, id)
                .then(usersService.delete(id, Version.of(version), agent))
                .then();
    }

    @Transactional
    public Mono<Void> renameUser(String userId, long version, String firstName, String lastName, Agent agent) {
        var id = UserId.of(userId);
        var name = FullName.of(
                FirstName.of(firstName),
                LastName.of(lastName)
        );

        return assertAgentIsAllowedTo(agent, RENAME, id)
                .then(usersService.rename(id, Version.of(version), name, agent))
                .then();
    }

    public Mono<UserDetails> getUserDetails(String userId, Agent agent) {
        var id = UserId.of(userId);

        return assertAgentIsAllowedTo(agent, READ, id)
                .then(usersService.getOrThrow(id))
                .map(user -> UserDetails.of(user.getId(), user.getName(), user.getMail()));
    }

    public Mono<Void> updateUserInLookup(String userId) {
        return usersService.getOrThrow(UserId.of(userId))
                .map(user -> LookupUser.of(user.getId(), user.getName(), user.getMail()))
                .flatMap(userLookupRepo::update);
    }

    public Mono<Void> removeUserFromLookup(String userId) {
        return userLookupRepo.remove(UserId.of(userId));
    }

    public Mono<Void> addPermissionsForNewUser(String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var userResource = Resource.of(ResourceType.of("USER"), ResourceId.of(userId));

        var readAsUser = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .on(userResource);
        var renameAsUser = Permission.builder()
                .holder(holder)
                .isAllowedTo(RENAME)
                .on(userResource);
        var deleteAsUser = Permission.builder()
                .holder(holder)
                .isAllowedTo(DELETE)
                .on(userResource);

        var readAsSystem = Permission.builder()
                .holder(Holder.group(HolderId.system()))
                .isAllowedTo(READ)
                .on(userResource);

        return permissionsService.addPermissions(
                readAsUser,
                renameAsUser,
                deleteAsUser,
                readAsSystem
        );
    }

    public Mono<Void> removePermissionsOnUser(String userId) {
        var userResource = Resource.of(ResourceType.of("USER"), ResourceId.of(userId));

        return permissionsService.removePermissionsByResource(userResource);
    }

    private Mono<Void> assertThatMailNotAlreadyInUse(Mail mail) {
        return userLookupRepo.findByMail(mail)
                .flatMap(user -> Mono.error(new MailAlreadyInUseError(mail.getValue())));
    }

    private Mono<Void> createDefaultUserIfNoneExists() {
        // TODO Configure default user via configuration file
        return userLookupRepo.count()
                .filter(count -> count == 0)
                .flatMap(count -> createUser("Default", "User", "default@kicherkrabbe.com", Agent.system()))
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
