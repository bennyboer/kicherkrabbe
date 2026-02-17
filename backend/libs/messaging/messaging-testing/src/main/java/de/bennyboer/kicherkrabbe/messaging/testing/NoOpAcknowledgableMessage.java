package de.bennyboer.kicherkrabbe.messaging.testing;

import de.bennyboer.kicherkrabbe.messaging.listener.AcknowledgableMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.amqp.core.Message;
import reactor.core.publisher.Mono;

@Getter
@AllArgsConstructor
public class NoOpAcknowledgableMessage implements AcknowledgableMessage {

    private final Message message;
    private final InMemoryMessageBus messageBus;

    @Override
    public Mono<Void> ack() {
        messageBus.messageHandled();
        return Mono.empty();
    }

    @Override
    public Mono<Void> nack(boolean requeue) {
        messageBus.messageHandled();
        return Mono.empty();
    }

    @Override
    public void nackSync(boolean requeue) {
        messageBus.messageHandled();
    }

}
