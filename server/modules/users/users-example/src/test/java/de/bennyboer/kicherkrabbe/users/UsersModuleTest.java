package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventWithMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.testing.persistence.MockReactiveTransactionManager;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.inmemory.InMemoryUserLookupRepo;
import de.bennyboer.kicherkrabbe.users.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.transaction.ReactiveTransactionManager;

import java.util.List;

public class UsersModuleTest {

    private final UsersModuleConfig config = new UsersModuleConfig();

    private final InMemoryUserLookupRepo usersLookupRepo = new InMemoryUserLookupRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final UsersService usersService = new UsersService(
            new InMemoryEventSourcingRepo(),
            eventPublisher
    );

    private final ReactiveTransactionManager transactionManager = new MockReactiveTransactionManager();

    private final UsersModule module = config.usersModule(usersService, usersLookupRepo, transactionManager);

    @BeforeEach
    public void setUp() {
        module.init();
    }

    public String createUser(String firstName, String lastName, String mail) {
        String userId = module.createUser(firstName, lastName, mail).block();
        updateUserInLookup(userId);

        return userId;
    }

    public void deleteUser(String userId) {
        module.deleteUser(userId).block();
        removeUserInLookup(userId);
    }

    public void renameUser(String userId, String firstName, String lastName) {
        module.renameUser(userId, firstName, lastName).block();
        updateUserInLookup(userId);
    }

    public UserDetails getUserDetails(String userId) {
        return module.getUserDetails(userId).block();
    }

    public void updateUserInLookup(String userId) {
        module.updateUserInLookup(userId).block();
    }

    public void removeUserInLookup(String userId) {
        module.removeUserFromLookup(userId).block();
    }

    public List<EventWithMetadata> findEventsByName(EventName eventName) {
        return eventPublisher.findEventsByName(eventName);
    }

}
