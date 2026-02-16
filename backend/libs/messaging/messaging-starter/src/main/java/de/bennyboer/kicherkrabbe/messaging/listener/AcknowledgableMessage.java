package de.bennyboer.kicherkrabbe.messaging.listener;

import com.rabbitmq.client.Channel;
import lombok.Value;
import org.springframework.amqp.core.Message;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Value
public class AcknowledgableMessage {

    Message message;

    Channel channel;

    public Mono<Void> ack() {
        return Mono.fromRunnable(() -> {
                    try {
                        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to acknowledge message", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    public Mono<Void> nack(boolean requeue) {
        return Mono.fromRunnable(() -> {
                    try {
                        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, requeue);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to nack message", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    public void nackSync(boolean requeue) {
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, requeue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to nack message", e);
        }
    }

}
