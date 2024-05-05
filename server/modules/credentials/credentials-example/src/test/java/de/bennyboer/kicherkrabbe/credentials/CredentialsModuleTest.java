package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.auth.password.PasswordEncoder;
import de.bennyboer.kicherkrabbe.auth.tokens.Token;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenGenerator;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.inmemory.InMemoryCredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.junit.jupiter.api.BeforeEach;
import reactor.core.publisher.Mono;

import java.util.List;

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

    private final PermissionsRepo permissionsRepo = new InMemoryPermissionsRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            permissionsRepo,
            event -> Mono.empty()
    );

    private final CredentialsModule module = config.credentialsModule(
            credentialsService,
            credentialsLookupRepo,
            permissionsService,
            tokenGenerator
    );

    @BeforeEach
    void setup() {
        PasswordEncoder.getInstance().enableTestProfile();
        module.initialize().block();
    }

    public String createCredentials(String name, String password, String userId, Agent agent) {
        String credentialsId = module.createCredentials(name, password, userId, agent).block();

        module.updateCredentialsInLookup(credentialsId).block();
        module.addPermissions(credentialsId, userId).block();

        return credentialsId;
    }

    public CredentialsModule.UseCredentialsResult useCredentials(String name, String password, Agent agent) {
        return module.useCredentials(name, password, agent).block();
    }

    public void deleteCredentials(String credentialsId, long version, Agent agent) {
        module.deleteCredentials(credentialsId, version, agent).block();

        module.removeCredentialsFromLookup(credentialsId).block();
        module.removePermissionsOnCredentials(credentialsId).block();
    }

    public void deleteCredentialsByUserId(String userId, Agent agent) {
        List<String> deletedCredentialIds = module.deleteCredentialsByUserId(userId, agent).collectList().block();

        for (String credentialsId : deletedCredentialIds) {
            module.removeCredentialsFromLookup(credentialsId).block();
            module.removePermissionsOnCredentials(credentialsId).block();
        }
    }

}
