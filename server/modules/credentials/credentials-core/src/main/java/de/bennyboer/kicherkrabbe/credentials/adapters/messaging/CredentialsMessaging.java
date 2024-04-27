package de.bennyboer.kicherkrabbe.credentials.adapters.messaging;

import de.bennyboer.kicherkrabbe.credentials.CredentialsModule;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.UUID.randomUUID;

@Slf4j
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

                    return module.updateCredentialsInLookup(credentialsId);
                }
        );
    }

    @Bean
    public EventListener onCredentialsDeletedUpdateLookupMsgListener(
            EventListenerFactory factory,
            CredentialsModule module
    ) {
        return factory.createEventListenerForEvent(
                "credentials-deleted-update-lookup",
                AggregateType.of("CREDENTIALS"),
                EventName.of("DELETED"),
                (metadata, version, payload) -> {
                    String credentialsId = metadata.getAggregateId().getValue();

                    return module.removeCredentialsFromLookup(credentialsId);
                }
        );
    }

    @Bean
    public EventListener onUserCreatedCreateCredentialsMsgListener(
            EventListenerFactory factory,
            CredentialsModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-created-create-credentials",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (metadata, version, payload) -> {
                    String userId = metadata.getAggregateId().getValue();
                    String mail = payload.get("mail").toString();
                    String initialPassword = randomUUID().toString();

                    if (mail.equals("default@kicherkrabbe.com")) {
                        log.info("Default users credentials are to be created with password '{}'", initialPassword);
                    }

                    return module.createCredentials(mail, initialPassword, userId).then();
                }
        );
    }

}
