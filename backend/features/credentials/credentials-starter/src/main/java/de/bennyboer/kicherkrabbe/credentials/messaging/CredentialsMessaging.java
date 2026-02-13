package de.bennyboer.kicherkrabbe.credentials.messaging;

import de.bennyboer.kicherkrabbe.credentials.CredentialsModule;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.UUID.randomUUID;

@Slf4j
@Configuration
public class CredentialsMessaging {

    @Bean("credentials_onCredentialsCreatedUpdateLookupMsgListener")
    public EventListener onCredentialsCreatedUpdateLookupMsgListener(
            EventListenerFactory factory,
            CredentialsModule module
    ) {
        return factory.createEventListenerForEvent(
                "credentials.credentials-created-update-lookup",
                AggregateType.of("CREDENTIALS"),
                EventName.of("CREATED"),
                (event) -> {
                    String credentialsId = event.getMetadata()
                            .getAggregateId()
                            .getValue();

                    return module.updateCredentialsInLookup(credentialsId);
                }
        );
    }

    @Bean("credentials_onCredentialsCreatedAddPermissionsMsgListener")
    public EventListener onCredentialsCreatedAddPermissionsMsgListener(
            EventListenerFactory factory,
            CredentialsModule module
    ) {
        return factory.createEventListenerForEvent(
                "credentials.credentials-created-add-permissions",
                AggregateType.of("CREDENTIALS"),
                EventName.of("CREATED"),
                (event) -> {
                    String credentialsId = event.getMetadata()
                            .getAggregateId()
                            .getValue();
                    String userId = event.getEvent()
                            .get("userId")
                            .toString();

                    return module.addPermissions(credentialsId, userId);
                }
        );
    }

    @Bean("credentials_onCredentialsDeletedUpdateLookupMsgListener")
    public EventListener onCredentialsDeletedUpdateLookupMsgListener(
            EventListenerFactory factory,
            CredentialsModule module
    ) {
        return factory.createEventListenerForEvent(
                "credentials.credentials-deleted-update-lookup",
                AggregateType.of("CREDENTIALS"),
                EventName.of("DELETED"),
                (event) -> {
                    String credentialsId = event.getMetadata()
                            .getAggregateId()
                            .getValue();

                    return module.removeCredentialsFromLookup(credentialsId);
                }
        );
    }

    @Bean("credentials_onCredentialsDeletedRemovePermissionsMsgListener")
    public EventListener onCredentialsDeletedRemovePermissionsMsgListener(
            EventListenerFactory factory,
            CredentialsModule module
    ) {
        return factory.createEventListenerForEvent(
                "credentials.credentials-deleted-remove-permissions",
                AggregateType.of("CREDENTIALS"),
                EventName.of("DELETED"),
                (event) -> {
                    String credentialsId = event.getMetadata()
                            .getAggregateId()
                            .getValue();

                    return module.removePermissionsOnCredentials(credentialsId);
                }
        );
    }

    @Bean("credentials_onUserCreatedCreateCredentialsMsgListener")
    public EventListener onUserCreatedCreateCredentialsMsgListener(
            EventListenerFactory factory,
            CredentialsModule module
    ) {
        return factory.createEventListenerForEvent(
                "credentials.user-created-create-credentials",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata()
                            .getAggregateId()
                            .getValue();
                    String mail = event.getEvent()
                            .get("mail")
                            .toString();
                    String initialPassword = randomUUID().toString();

                    if (mail.equals("default@kicherkrabbe.com")) {
                        log.info("Default users credentials are to be created with password '{}'", initialPassword);
                    }

                    return module.createCredentials(mail, initialPassword, userId, Agent.system()).then();
                }
        );
    }

    @Bean("credentials_onUserDeletedDeleteCredentialsMsgListener")
    public EventListener onUserDeletedDeleteCredentialsMsgListener(
            EventListenerFactory factory,
            CredentialsModule module
    ) {
        return factory.createEventListenerForEvent(
                "credentials.user-deleted-delete-credentials",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata()
                            .getAggregateId()
                            .getValue();

                    return module.deleteCredentialsByUserId(userId, Agent.system()).then();
                }
        );
    }

}
