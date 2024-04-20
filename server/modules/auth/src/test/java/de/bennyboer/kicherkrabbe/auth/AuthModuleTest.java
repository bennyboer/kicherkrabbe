package de.bennyboer.kicherkrabbe.auth;

import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsService;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.password.PasswordEncoder;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.junit.jupiter.api.BeforeEach;

public class AuthModuleTest {

    protected final TestClock clock = new TestClock();

    private final InMemoryEventSourcingRepo credentialsEventSourcingRepo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher credentialsEventPublisher = new LoggingEventPublisher();

    private final CredentialsService credentialsService = new CredentialsService(
            credentialsEventSourcingRepo,
            credentialsEventPublisher,
            clock
    );

    private final AuthModuleConfig config = new AuthModuleConfig();

    private final AuthModule module = config.authModule(credentialsService);

    @BeforeEach
    void setup() {
        PasswordEncoder.getInstance().enableTestProfile();
    }

    public void createCredentials(String name, String password, String userId) {
        module.createCredentials(name, password, userId).block();
    }

    public AuthModule.UseCredentialsResult useCredentials(String name, String password) {
        return module.useCredentials(name, password).block();
    }

}
