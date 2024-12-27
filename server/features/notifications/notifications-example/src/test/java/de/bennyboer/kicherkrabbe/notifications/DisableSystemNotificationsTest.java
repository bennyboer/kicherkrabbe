package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.notifications.settings.SystemNotificationsAlreadyDisabledException;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DisableSystemNotificationsTest extends NotificationsModuleTest {

    @Test
    void shouldDisableSystemNotifications() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: the user enabled the system notifications
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var version = enableSystemNotifications(settings.settings.version, Agent.user(AgentId.of("USER_ID"))).version;

        // when: disabling the system notifications
        disableSystemNotifications(version, Agent.user(AgentId.of("USER_ID")));

        // then: the system notifications are disabled
        settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        assertThat(settings.settings.version).isEqualTo(2L);
        assertThat(settings.settings.systemSettings.enabled).isFalse();
    }

    @Test
    void shouldRaiseErrorIfSystemNotificationsAreAlreadyDisabled() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // when: disabling the system notifications; then: an exception is thrown
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        assertThatThrownBy(() -> disableSystemNotifications(
                settings.settings.version,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e instanceof SystemNotificationsAlreadyDisabledException);
    }

    @Test
    void shouldNotDisableSystemNotificationsWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to manage settings

        // when: the user disables system notifications; then: an exception is thrown
        assertThatThrownBy(() -> disableSystemNotifications(0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseExceptionGivenAnOutdatedVersion() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: the user enabled the system notifications
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        enableSystemNotifications(settings.settings.version, Agent.user(AgentId.of("USER_ID")));

        // and: the user disabled the system notifications
        disableSystemNotifications(settings.settings.version + 1, Agent.user(AgentId.of("USER_ID")));

        // when: disabling the system notifications with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> disableSystemNotifications(1L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
