package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QuerySettingsTest extends MailingModuleTest {

    @Test
    void shouldQuerySettings() {
        // given: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // when: querying the settings
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));

        // then: the response contains the expected settings
        assertThat(settings.settings.version).isEqualTo(0L);
        assertThat(settings.settings.rateLimit.durationInMs).isEqualTo(24 * 60 * 60 * 1000);
        assertThat(settings.settings.rateLimit.limit).isEqualTo(100);
        assertThat(settings.settings.mailgun.maskedApiToken).isNull();
    }

    @Test
    void shouldNotQuerySettingsWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to read settings

        // when: the user queries the settings; then: an exception is thrown
        assertThatThrownBy(() -> getSettings(Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
