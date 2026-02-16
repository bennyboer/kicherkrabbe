package de.bennyboer.kicherkrabbe.messaging.listener;

import org.springframework.amqp.core.Message;
import reactor.core.publisher.Mono;

public interface AcknowledgableMessage {

    Message getMessage();

    Mono<Void> ack();

    Mono<Void> nack(boolean requeue);

    void nackSync(boolean requeue);

}
