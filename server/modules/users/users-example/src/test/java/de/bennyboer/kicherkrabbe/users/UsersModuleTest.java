package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventWithMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.inmemory.InMemoryUserLookupRepo;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public class UsersModuleTest {

    private final UsersModuleConfig config = new UsersModuleConfig();

    private final InMemoryUserLookupRepo usersLookupRepo = new InMemoryUserLookupRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final UsersService usersService = new UsersService(
            new InMemoryEventSourcingRepo(),
            eventPublisher
    );

    private final ReactiveTransactionManager transactionManager = new MockReactiveTransactionManager();

    private final PermissionsRepo permissionsRepo = new InMemoryPermissionsRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            permissionsRepo,
            ignored -> Mono.empty()
    );

    private final UsersModule module = config.usersModule(
            usersService,
            usersLookupRepo,
            permissionsService,
            transactionManager
    );

    @BeforeEach
    public void setUp() {
        module.initialize().block();
    }

    public String createUser(String firstName, String lastName, String mail, Agent agent) {
        String userId = module.createUser(firstName, lastName, mail, agent).block();

        updateUserInLookup(userId);
        addPermissionsForNewUser(userId);

        return userId;
    }

    public void deleteUser(String userId, Agent agent) {
        module.deleteUser(userId, agent).block();

        removeUserInLookup(userId);
        removePermissionsOnUser(userId);
    }

    public void renameUser(String userId, String firstName, String lastName, Agent agent) {
        module.renameUser(userId, firstName, lastName, agent).block();
        updateUserInLookup(userId);
    }

    public UserDetails getUserDetails(String userId) {
        return module.getUserDetails(userId).block();
    }

    public List<EventWithMetadata> findEventsByName(EventName eventName) {
        return eventPublisher.findEventsByName(eventName);
    }

    public void userHasPermission(UserId allowedUserId, Action action) {
        userHasPermission(allowedUserId, action, null);
    }

    public void userHasPermission(UserId allowedUserId, Action action, @Nullable UserId targetUserId) {
        ResourceType resourceType = ResourceType.of("USER");

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(Holder.user(HolderId.of(allowedUserId.getValue())))
                .isAllowedTo(action);

        Permission permission = Optional.ofNullable(targetUserId)
                .map(tUId -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(tUId.getValue()))))
                .orElseGet(() -> permissionBuilder.onType(resourceType));

        permissionsService.addPermission(permission).block();
    }

    private void updateUserInLookup(String userId) {
        module.updateUserInLookup(userId).block();
    }

    private void removeUserInLookup(String userId) {
        module.removeUserFromLookup(userId).block();
    }

    private void addPermissionsForNewUser(String userId) {
        module.addPermissionsForNewUser(userId).block();
    }

    private void removePermissionsOnUser(String userId) {
        module.removePermissionsOnUser(userId).block();
    }

}
