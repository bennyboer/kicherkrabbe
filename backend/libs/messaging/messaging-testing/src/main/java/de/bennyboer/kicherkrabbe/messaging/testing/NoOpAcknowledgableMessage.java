package de.bennyboer.kicherkrabbe.messaging.testing;

import de.bennyboer.kicherkrabbe.messaging.listener.AcknowledgableMessage;
import org.springframework.amqp.core.Message;
import reactor.core.publisher.Mono;

public class NoOpAcknowledgableMessage extends AcknowledgableMessage {

    public NoOpAcknowledgableMessage(Message message) {
        super(message, null);
    }

    @Override
    public Mono<Void> ack() {
        return Mono.empty();
    }

    @Override
    public Mono<Void> nack(boolean requeue) {
        return Mono.empty();
    }

    @Override
    public void nackSync(boolean requeue) {
    }

}
