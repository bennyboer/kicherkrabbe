package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.notifications.api.ChannelDTO;
import de.bennyboer.kicherkrabbe.notifications.api.ChannelTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.api.requests.UpdateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateSystemChannelTest extends NotificationsModuleTest {

    @Test
    void shouldUpdateSystemChannel() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // when: updating a system channel
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var request = new UpdateSystemChannelRequest();
        request.version = settings.settings.version;
        request.channel = new ChannelDTO();
        request.channel.type = ChannelTypeDTO.EMAIL;
        request.channel.mail = "john.doe@kicherkrabbe.com";
        updateSystemChannel(request, Agent.user(AgentId.of("USER_ID")));

        // then: the system channel is updated
        settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        assertThat(settings.settings.version).isEqualTo(1L);
        assertThat(settings.settings.systemSettings.channels).hasSize(1);

        var channel = settings.settings.systemSettings.channels.stream().findFirst().orElseThrow();
        assertThat(channel.active).isFalse();
        assertThat(channel.channel.type).isEqualTo(ChannelTypeDTO.EMAIL);
        assertThat(channel.channel.mail).isEqualTo("john.doe@kicherkrabbe.com");
    }

    @Test
    void shouldNotUpdateSystemChannelWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to manage settings

        // when: the user enables system notifications; then: an exception is thrown
        var request = new UpdateSystemChannelRequest();
        request.version = 0L;
        request.channel = new ChannelDTO();
        request.channel.type = ChannelTypeDTO.EMAIL;
        request.channel.mail = "john.doe@kicherkrabbe.com";
        assertThatThrownBy(() -> updateSystemChannel(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseExceptionGivenAnOutdatedVersion() {
        // given: the current user is allowed to read notifications and manage settings
        allowUserToReadNotificationsAndManageSettings("USER_ID");

        // and: the user enabled system notifications
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var version = enableSystemNotifications(settings.settings.version, Agent.user(AgentId.of("USER_ID")));

        // when: updating a system channel with an outdated version; then: an exception is thrown
        var request = new UpdateSystemChannelRequest();
        request.version = 0L;
        request.channel = new ChannelDTO();
        request.channel.type = ChannelTypeDTO.EMAIL;
        request.channel.mail = "john.doe@kicherkrabbe.com";
        assertThatThrownBy(() -> updateSystemChannel(request, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
