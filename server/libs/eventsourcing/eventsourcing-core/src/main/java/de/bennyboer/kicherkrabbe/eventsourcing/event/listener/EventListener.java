package de.bennyboer.kicherkrabbe.eventsourcing.event.listener;

import de.bennyboer.kicherkrabbe.messaging.listener.MessageListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EventListener {

    private final MessageListener messageListener;

    @PostConstruct
    public void init() {
        messageListener.start();
    }

    @PreDestroy
    public void destroy() {
        messageListener.destroy();
    }

}
