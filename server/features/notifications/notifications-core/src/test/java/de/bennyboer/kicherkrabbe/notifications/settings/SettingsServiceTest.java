package de.bennyboer.kicherkrabbe.notifications.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;
import de.bennyboer.kicherkrabbe.notifications.channel.mail.EMail;
import de.bennyboer.kicherkrabbe.notifications.channel.telegram.Telegram;
import de.bennyboer.kicherkrabbe.notifications.channel.telegram.TelegramChatId;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SettingsServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final SettingsService settingsService = new SettingsService(
            repo,
            eventPublisher,
            Clock.systemUTC()
    );

    @Test
    void shouldInitSettings() {
        // when: initializing settings
        var settingsId = init();

        // then: the settings are initialized
        var settings = get(settingsId);
        assertThat(settings.getId()).isEqualTo(settingsId);
        assertThat(settings.getVersion()).isEqualTo(Version.zero());
        assertThat(settings.getSystemSettings()).isEqualTo(SystemSettings.init());
    }

    @Test
    void shouldEnableSystemNotifications() {
        // given: initialized settings
        var settingsId = init();

        // when: enabling system notifications
        var version = enableSystemNotifications(settingsId, Version.zero());

        // then: system notifications are enabled
        var settings = get(settingsId);
        assertThat(settings.getVersion()).isEqualTo(version);
        assertThat(settings.getSystemSettings().isEnabled()).isTrue();
    }

    @Test
    void shouldNotEnableSystemNotificationsGivenAnOutdatedVersion() {
        // given: disabled initialized settings
        var settingsId = init();
        enableSystemNotifications(settingsId, Version.zero());
        disableSystemNotifications(settingsId, Version.of(1));

        // when: enabling system notifications with an outdated version
        assertThatThrownBy(() -> enableSystemNotifications(settingsId, Version.of(1)))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotEnableSystemNotificationsIfAlreadyEnabled() {
        // given: enabled system notifications
        var settingsId = init();
        enableSystemNotifications(settingsId, Version.zero());

        // when: enabling system notifications again
        assertThatThrownBy(() -> enableSystemNotifications(settingsId, Version.of(1)))
                .matches(e -> e instanceof SystemNotificationsAlreadyEnabledException);
    }

    @Test
    void shouldDisableSystemNotifications() {
        // given: enabled system notifications
        var settingsId = init();
        var version = enableSystemNotifications(settingsId, Version.zero());

        // when: disabling system notifications
        var newVersion = disableSystemNotifications(settingsId, version);

        // then: system notifications are disabled
        var settings = get(settingsId);
        assertThat(settings.getVersion()).isEqualTo(newVersion);
        assertThat(settings.getSystemSettings().isEnabled()).isFalse();
    }

    @Test
    void shouldNotDisableSystemNotificationsGivenAnOutdatedVersion() {
        // given: enabled system notifications
        var settingsId = init();
        enableSystemNotifications(settingsId, Version.zero());

        // when: disabling system notifications with an outdated version
        assertThatThrownBy(() -> disableSystemNotifications(settingsId, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotDisableSystemNotificationsIfAlreadyDisabled() {
        // given: disabled system notifications
        var settingsId = init();
        enableSystemNotifications(settingsId, Version.zero());
        disableSystemNotifications(settingsId, Version.of(1));

        // when: disabling system notifications again
        assertThatThrownBy(() -> disableSystemNotifications(settingsId, Version.of(2)))
                .matches(e -> e instanceof SystemNotificationsAlreadyDisabledException);
    }

    @Test
    void shouldUpdateSystemChannel() {
        // given: initialized settings
        var settingsId = init();

        // when: updating the mail system channel
        var version = updateSystemChannel(
                settingsId,
                Version.zero(),
                Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))
        );

        // then: the system mail channel is updated
        var settings = get(settingsId);
        assertThat(settings.getVersion()).isEqualTo(version);
        assertThat(settings.getSystemSettings().getChannels()).hasSize(1);
        assertThat(settings.getSystemSettings().getChannels()).contains(
                ActivatableChannel.of(Channel.mail(EMail.of("john.doe@kicherkrabbe.com")), false)
        );

        // when: updating the telegram system channel
        version = updateSystemChannel(
                settingsId,
                version,
                Channel.telegram(Telegram.of(TelegramChatId.of("1234567890")))
        );

        // then: the system telegram channel is updated
        settings = get(settingsId);
        assertThat(settings.getVersion()).isEqualTo(version);
        assertThat(settings.getSystemSettings().getChannels()).hasSize(2);
        assertThat(settings.getSystemSettings().getChannels()).contains(
                ActivatableChannel.of(Channel.mail(EMail.of("john.doe@kicherkrabbe.com")), false),
                ActivatableChannel.of(Channel.telegram(Telegram.of(TelegramChatId.of("1234567890"))), false)
        );
    }

    @Test
    void shouldNotUpdateSystemChannelGivenAnOutdatedVersion() {
        // given: some settings
        var settingsId = init();
        enableSystemNotifications(settingsId, Version.zero());

        // when: updating a system channel with an outdated version
        assertThatThrownBy(() -> updateSystemChannel(
                settingsId,
                Version.zero(),
                Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldActivateSystemChannel() {
        // given: initialized settings
        var settingsId = init();
        var version = updateSystemChannel(
                settingsId,
                Version.zero(),
                Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))
        );

        // when: activating the email system channel
        version = activateSystemChannel(settingsId, version, ChannelType.EMAIL);

        // then: the email system channel is activated
        var settings = get(settingsId);
        assertThat(settings.getVersion()).isEqualTo(version);
        assertThat(settings.getSystemSettings().getChannels()).hasSize(1);
        assertThat(settings.getSystemSettings().getChannels()).contains(
                ActivatableChannel.of(Channel.mail(EMail.of("john.doe@kicherkrabbe.com")), true)
        );
    }

    @Test
    void shouldNotActivateSystemChannelGivenAnOutdatedVersion() {
        // given: some settings
        var settingsId = init();
        var version = enableSystemNotifications(settingsId, Version.zero());
        version = updateSystemChannel(
                settingsId,
                version,
                Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))
        );

        // when: activating the email system channel with an outdated version
        assertThatThrownBy(() -> activateSystemChannel(settingsId, Version.of(1), ChannelType.EMAIL))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDeactivateSystemChannel() {
        // given: initialized settings
        var settingsId = init();
        var version = updateSystemChannel(
                settingsId,
                Version.zero(),
                Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))
        );

        // and: activated email system channel
        version = activateSystemChannel(settingsId, version, ChannelType.EMAIL);

        // when: deactivating the email system channel
        version = deactivateSystemChannel(settingsId, version, ChannelType.EMAIL);

        // then: the email system channel is deactivated
        var settings = get(settingsId);
        assertThat(settings.getVersion()).isEqualTo(version);
        assertThat(settings.getSystemSettings().getChannels()).hasSize(1);
        assertThat(settings.getSystemSettings().getChannels()).contains(
                ActivatableChannel.of(Channel.mail(EMail.of("john.doe@kicherkrabbe.com")), false)
        );
    }

    @Test
    void shouldNotDeactivateSystemChannelGivenAnOutdatedVersion() {
        // given: some settings
        var settingsId = init();
        var version = enableSystemNotifications(settingsId, Version.zero());
        version = updateSystemChannel(
                settingsId,
                version,
                Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))
        );

        // and: activated email system channel
        version = activateSystemChannel(settingsId, version, ChannelType.EMAIL);

        // when: deactivating the email system channel with an outdated version
        assertThatThrownBy(() -> deactivateSystemChannel(settingsId, Version.zero(), ChannelType.EMAIL))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotActivateSystemChannelGivenAnUnknownChannelType() {
        // given: some settings
        var settingsId = init();
        var version = enableSystemNotifications(settingsId, Version.zero());
        version = updateSystemChannel(
                settingsId,
                version,
                Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))
        );

        // when: activating an unknown channel type
        assertThatThrownBy(() -> activateSystemChannel(settingsId, Version.of(2), ChannelType.UNKNOWN))
                .matches(e -> e instanceof ChannelUnavailableException);
    }

    @Test
    void shouldNotActivateSystemChannelIfAlreadyActivated() {
        // given: some settings
        var settingsId = init();
        var version = enableSystemNotifications(settingsId, Version.zero());
        version = updateSystemChannel(
                settingsId,
                version,
                Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))
        );

        // and: the email channel is activated
        version = activateSystemChannel(settingsId, version, ChannelType.EMAIL);

        // when: activating the email channel again
        assertThatThrownBy(() -> activateSystemChannel(settingsId, Version.of(3), ChannelType.EMAIL))
                .matches(e -> e instanceof ChannelAlreadyActivatedException);
    }

    @Test
    void shouldNotDeactivateSystemChannelGivenAnUnknownChannelType() {
        // given: some settings
        var settingsId = init();
        var version = enableSystemNotifications(settingsId, Version.zero());
        version = updateSystemChannel(
                settingsId,
                version,
                Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))
        );

        // when: deactivating an unknown channel type
        assertThatThrownBy(() -> deactivateSystemChannel(settingsId, Version.of(2), ChannelType.UNKNOWN))
                .matches(e -> e instanceof ChannelUnavailableException);
    }

    @Test
    void shouldNotDeactivateSystemChannelIfAlreadyDeactivated() {
        // given: some settings
        var settingsId = init();
        var version = enableSystemNotifications(settingsId, Version.zero());
        version = updateSystemChannel(
                settingsId,
                version,
                Channel.mail(EMail.of("john.doe@kicherkrabbe.com"))
        );

        // and: the email channel is deactivated

        // when: deactivating the email channel again
        assertThatThrownBy(() -> deactivateSystemChannel(settingsId, Version.of(3), ChannelType.EMAIL))
                .matches(e -> e instanceof ChannelAlreadyDeactivatedException);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: initialized settings
        var settingsId = init();

        // when: enabling and disabling system notifications 100 times
        var version = Version.zero();
        for (int i = 0; i < 100; i++) {
            version = enableSystemNotifications(settingsId, version);
            version = disableSystemNotifications(settingsId, version);
        }

        // then: the settings are disabled
        var settings = get(settingsId);
        assertThat(settings.getVersion()).isEqualTo(Version.of(202));
        assertThat(settings.getSystemSettings().isEnabled()).isFalse();

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(settingsId.getValue()),
                Settings.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private SettingsId init() {
        return settingsService.init(Agent.system()).block().getId();
    }

    private Settings get(SettingsId id) {
        return settingsService.get(id).block();
    }

    private Version enableSystemNotifications(SettingsId id, Version version) {
        return settingsService.enableSystemNotifications(id, version, Agent.system()).block();
    }

    private Version disableSystemNotifications(SettingsId id, Version version) {
        return settingsService.disableSystemNotifications(id, version, Agent.system()).block();
    }

    private Version updateSystemChannel(SettingsId id, Version version, Channel channel) {
        return settingsService.updateSystemChannel(id, version, channel, Agent.system()).block();
    }

    private Version activateSystemChannel(SettingsId id, Version version, ChannelType channelType) {
        return settingsService.activateSystemChannel(id, version, channelType, Agent.system()).block();
    }

    private Version deactivateSystemChannel(SettingsId id, Version version, ChannelType channelType) {
        return settingsService.deactivateSystemChannel(id, version, channelType, Agent.system()).block();
    }

}
