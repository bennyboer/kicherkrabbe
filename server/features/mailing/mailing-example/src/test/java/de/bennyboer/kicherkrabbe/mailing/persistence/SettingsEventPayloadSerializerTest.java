package de.bennyboer.kicherkrabbe.mailing.persistence;

import de.bennyboer.kicherkrabbe.mailing.settings.ApiToken;
import de.bennyboer.kicherkrabbe.mailing.settings.MailgunSettings;
import de.bennyboer.kicherkrabbe.mailing.settings.RateLimitSettings;
import de.bennyboer.kicherkrabbe.mailing.settings.init.InitEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.clear.MailgunApiTokenClearedEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.update.MailgunApiTokenUpdatedEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.ratelimit.update.RateLimitUpdatedEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SettingsEventPayloadSerializerTest {

    private final SettingsEventPayloadSerializer serializer = new SettingsEventPayloadSerializer();

    @Test
    void shouldSerializeAndDeserializeInitEvent() {
        // when: an init event is serialized
        var event = InitEvent.of(
                RateLimitSettings.of(Duration.ofSeconds(10), 100),
                MailgunSettings.of(ApiToken.of("SOME_API_TOKEN"))
        );
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "rateLimit", Map.of(
                        "duration", "PT10S",
                        "limit", 100L
                ),
                "mailgun", Map.of(
                        "apiToken", "SOME_API_TOKEN"
                )
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(InitEvent.NAME, InitEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeRateLimitUpdatedEvent() {
        // when: a rate limit updated event is serialized
        var event = RateLimitUpdatedEvent.of(
                Duration.ofSeconds(10),
                100
        );
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "duration", "PT10S",
                "limit", 100L
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(
                RateLimitUpdatedEvent.NAME,
                RateLimitUpdatedEvent.VERSION,
                serialized
        );

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeMailgunApiTokenUpdatedEvent() {
        // when: a mailgun api token updated event is serialized
        var event = MailgunApiTokenUpdatedEvent.of(
                ApiToken.of("SOME_API_TOKEN")
        );
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "apiToken", "SOME_API_TOKEN"
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(
                MailgunApiTokenUpdatedEvent.NAME,
                MailgunApiTokenUpdatedEvent.VERSION,
                serialized
        );

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeMailgunApiTokenClearedEvent() {
        // when: a mailgun api token cleared event is serialized
        var event = MailgunApiTokenClearedEvent.of();
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEmpty();

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(
                MailgunApiTokenClearedEvent.NAME,
                MailgunApiTokenClearedEvent.VERSION,
                serialized
        );

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

}
