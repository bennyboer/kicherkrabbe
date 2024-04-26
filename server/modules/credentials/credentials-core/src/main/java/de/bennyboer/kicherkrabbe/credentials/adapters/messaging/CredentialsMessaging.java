package de.bennyboer.kicherkrabbe.credentials.adapters.messaging;

import de.bennyboer.kicherkrabbe.credentials.CredentialsModule;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CredentialsMessaging {

    @Bean
    public EventListener onCredentialsCreatedUpdateLookupMsgListener(
            EventListenerFactory factory,
            CredentialsModule module
    ) {
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
