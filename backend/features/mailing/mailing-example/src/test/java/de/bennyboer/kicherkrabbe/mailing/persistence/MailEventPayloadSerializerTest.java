package de.bennyboer.kicherkrabbe.mailing.persistence;

import de.bennyboer.kicherkrabbe.mailing.mail.Receiver;
import de.bennyboer.kicherkrabbe.mailing.mail.Sender;
import de.bennyboer.kicherkrabbe.mailing.mail.Subject;
import de.bennyboer.kicherkrabbe.mailing.mail.Text;
import de.bennyboer.kicherkrabbe.mailing.mail.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.mailing.mail.send.SentEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.EMail;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.mailing.MailingService.MAILGUN;
import static org.assertj.core.api.Assertions.assertThat;

public class MailEventPayloadSerializerTest {

    private final MailEventPayloadSerializer serializer = new MailEventPayloadSerializer();

    @Test
    void shouldSerializeAndDeserializeSentEvent() {
        // when: a sent event is serialized
        var event = SentEvent.of(
                Sender.of(EMail.of("john.doe@kicherkrabbe.com")),
                Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com"))),
                Subject.of("Hello"),
                Text.of("Hello, Jane!"),
                MAILGUN
        );
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "sender", Map.of(
                        "mail", "john.doe@kicherkrabbe.com"
                ),
                "receivers", List.of(
                        Map.of(
                                "mail", "jane.doe@kicherkrabbe.com"
                        )
                ),
                "subject", "Hello",
                "text", "Hello, Jane!",
                "mailingService", "MAILGUN"
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(SentEvent.NAME, SentEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeDeletedEvent() {
        // when: a deleted event is serialized
        var event = DeletedEvent.of();
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEmpty();

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(DeletedEvent.NAME, DeletedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

}
