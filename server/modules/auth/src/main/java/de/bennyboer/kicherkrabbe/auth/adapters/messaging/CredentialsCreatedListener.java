package de.bennyboer.kicherkrabbe.auth.adapters.messaging;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListener;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;

// TODO Use event sourcing message listener instead - should be simpler
public class CredentialsCreatedListener {

    private final MessageListener listener;

    public CredentialsCreatedListener(MessageListener listener) {
        this.listener = listener;
    }

    private Disposable disposable;

    @PostConstruct
    public void listen() {
        disposable = listener.listen(
                        ExchangeTarget.of("credentials"),
                        RoutingKey.parse("events.created"),
                        "test"
                )
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(delivery -> {
                    String content = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    System.out.println("Received: " + content);

                    delivery.ack();
                });
    }

    @PreDestroy
    public void close() {
        disposable.dispose();
    }

}
