package de.bennyboer.kicherkrabbe.messaging;

import com.rabbitmq.client.Connection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.core.publisher.Mono;

@Getter
@AllArgsConstructor
public class ConnectionContainer {

    private final Mono<Connection> connectionMono;

    public void destroy() throws Exception {
        connectionMono.block().close();
    }

}
