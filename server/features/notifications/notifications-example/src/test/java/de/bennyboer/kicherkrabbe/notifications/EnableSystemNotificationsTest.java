package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.notifications.settings.SystemNotificationsAlreadyEnabledException;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EnableSystemNotificationsTest extends NotificationsModuleTest {

    @Test
    void shouldEnableSystemNotifications() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // when: enabling the system notifications
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        enableSystemNotifications(settings.settings.version, Agent.user(AgentId.of("USER_ID")));

        // then: the system notifications are enabled
        settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        assertThat(settings.settings.version).isEqualTo(1L);
        assertThat(settings.settings.systemSettings.enabled).isTrue();
    }

    @Test
    void shouldRaiseErrorWhenSystemNotificationsAreAlreadyEnabled() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: the user enabled the system notifications
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var version = enableSystemNotifications(settings.settings.version, Agent.user(AgentId.of("USER_ID"))).version;

        // when: enabling the system notifications again; then: an exception is thrown
        assertThatThrownBy(() -> enableSystemNotifications(version, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e instanceof SystemNotificationsAlreadyEnabledException);
    }

    @Test
    void shouldNotEnableSystemNotificationsWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to manage settings

        // when: the user enables system notifications; then: an exception is thrown
        assertThatThrownBy(() -> enableSystemNotifications(0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseExceptionGivenAnOutdatedVersion() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: the user enabled the system notifications
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        enableSystemNotifications(settings.settings.version, Agent.user(AgentId.of("USER_ID")));

        // when: enabling the system notifications with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> enableSystemNotifications(0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
