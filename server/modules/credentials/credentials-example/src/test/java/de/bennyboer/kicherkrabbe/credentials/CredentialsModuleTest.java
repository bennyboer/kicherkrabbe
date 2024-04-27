package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.auth.password.PasswordEncoder;
import de.bennyboer.kicherkrabbe.auth.tokens.Token;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenGenerator;
import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.inmemory.InMemoryCredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.internal.CredentialsService;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.junit.jupiter.api.BeforeEach;
import reactor.core.publisher.Mono;

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

    private final TokenGenerator tokenGenerator =
            content -> Mono.just(Token.of("token-for-%s".formatted(content.getOwner()
                    .getId()
                    .getValue())));

    private final CredentialsModule module = config.credentialsModule(
            credentialsService,
            credentialsLookupRepo,
            tokenGenerator
    );

    @BeforeEach
    void setup() {
        PasswordEncoder.getInstance().enableTestProfile();
    }

    public String createCredentials(String name, String password, String userId) {
        String credentialsId = module.createCredentials(name, password, userId).block();
        module.updateCredentialsInLookup(credentialsId).block();

        return credentialsId;
    }

    public CredentialsModule.UseCredentialsResult useCredentials(String name, String password) {
        return module.useCredentials(name, password).block();
    }

    public void deleteCredentials(String credentialsId) {
        module.deleteCredentials(credentialsId).block();
        module.removeCredentialsFromLookup(credentialsId).block();
    }

}
