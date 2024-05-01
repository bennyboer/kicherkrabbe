package de.bennyboer.kicherkrabbe.eventsourcing;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventSourcingConfig {

    @Bean
    public EventListenerFactory eventListenerFactory(
            MessageListenerFactory messageListenerFactory,
            @Qualifier("messagingObjectMapper") ObjectMapper objectMapper
    ) {
        return new EventListenerFactory(messageListenerFactory, objectMapper);
    }

}
