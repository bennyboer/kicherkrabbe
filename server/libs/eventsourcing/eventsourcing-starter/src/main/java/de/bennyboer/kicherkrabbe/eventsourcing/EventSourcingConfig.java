package de.bennyboer.kicherkrabbe.eventsourcing;

import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class EventSourcingConfig {

    @Bean
    public EventListenerFactory eventListenerFactory(
            MessageListenerFactory messageListenerFactory,
            @Qualifier("messagingJsonMapper") JsonMapper jsonMapper
    ) {
        return new EventListenerFactory(messageListenerFactory, jsonMapper);
    }

}
