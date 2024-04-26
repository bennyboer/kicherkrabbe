package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.auth.password.PasswordEncoder;
import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.inmemory.InMemoryCredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.internal.CredentialsService;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.junit.jupiter.api.BeforeEach;

public class CredentialsModuleTest {

    protected final TestClock clock = new TestClock();

    private final InMemoryEventSourcingRepo credentialsEventSourcingRepo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher credentialsEventPublisher = new LoggingEventPublisher();

    private final CredentialsService credentialsService = new CredentialsService(
            credentialsEventSourcingRepo,
            credentialsEventPublisher,
            clock
    );

    private final CredentialsLookupRepo credentialsLookupRepo = new InMemoryCredentialsLookupRepo();

    private final CredentialsModuleConfig config = new CredentialsModuleConfig();

    private final CredentialsModule module = config.credentialsModule(credentialsService, credentialsLookupRepo);

    @BeforeEach
    void setup() {
        PasswordEncoder.getInstance().enableTestProfile();
    }

    public void createCredentials(String name, String password, String userId) {
        String credentialsId = module.createCredentials(name, password, userId).block();
        module.updateCredentialsInLookup(credentialsId).block();
    }

    public CredentialsModule.UseCredentialsResult useCredentials(String name, String password) {
        return module.useCredentials(name, password).block();
    }

}
