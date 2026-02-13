package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.credentials.create.NameAlreadyTakenError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateCredentialsTest extends CredentialsModuleTest {

    @Test
    void shouldCreateCredentials() {
        // when: creating some credentials
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // then: the credentials can be used
        var result = useCredentials("TestName", "TestPassword", Agent.anonymous());
        assertThat(result.getAccessToken()).isEqualTo("token-for-USER_ID");
    }

    @Test
    void shouldFailWhenTryingToCreateCredentialsWithTheSameName() {
        // given: some credentials
        createCredentials("TestName", "TestPassword", "USER_ID", Agent.system());

        // when: trying to create credentials with the same name; then: an error is raised
        assertThatThrownBy(() -> createCredentials("TestName", "TestPassword", "USER_ID", Agent.system()))
                .matches(e -> e.getCause() instanceof NameAlreadyTakenError
                        && e.getCause().getMessage().equals("Name already taken: TestName"));
    }

    @Test
    void shouldNotBeAbleToCreateCredentialsAsAnonymousUser() {
        // when: creating some credentials as anonymous user; then: an error is raised
        assertThatThrownBy(() -> createCredentials("TestName", "TestPassword", "USER_ID", Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotBeAbleToCreateCredentialsAsUser() {
        // when: creating some credentials as user; then: an error is raised
        assertThatThrownBy(() -> createCredentials(
                "TestName",
                "TestPassword",
                "USER_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
