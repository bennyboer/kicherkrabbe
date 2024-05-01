package de.bennyboer.kicherkrabbe.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.messaging.testing.BaseMessagingTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;

public class MessageListenerTest extends BaseMessagingTest {

    @Autowired
    public MessageListenerTest(
            MessageListenerFactory factory,
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager,
            ObjectMapper objectMapper
    ) {
        super(factory, outbox, transactionManager, objectMapper);
    }

    @Test
    void shouldReceiveMessage() {
        // given: a message to be sent
        var message = "Hello World!";

        // and: a message listener is registered
        var message$ = receive("test", "events.test")
                .take(1)
                .timeout(Duration.ofSeconds(10));

        // when: the message is sent
        send("test", "events.test", Map.of("message", message));

        // then: the message is received
        StepVerifier.create(message$)
                .expectNext(Map.of("message", message))
                .verifyComplete();
    }

}
