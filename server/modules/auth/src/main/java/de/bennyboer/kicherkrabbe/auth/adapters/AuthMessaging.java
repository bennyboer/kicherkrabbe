package de.bennyboer.kicherkrabbe.auth.adapters;

import de.bennyboer.kicherkrabbe.auth.AuthModule;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthMessaging {

    @Bean
    public EventListener onCredentialsCreatedUpdateLookupMsgListener(EventListenerFactory factory, AuthModule module) {
        return factory.createEventListenerForEvent(
                "credentials-created-update-lookup",
                AggregateType.of("CREDENTIALS"),
                EventName.of("CREATED"),
                (metadata, version, payload) -> {
                    String credentialsId = metadata.getAggregateId().getValue();
                    System.out.println("credentialsId = " + credentialsId);

                    return module.updateCredentialsInLookup(credentialsId);
                }
        );
    }

}
